package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.category.SkyCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.ksp.BuildingCompanion
import io.github.flyingpig525.ksp.PlayerBuildings
import io.github.flyingpig525.serialization.PointSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.bladehunt.kotstom.extension.y
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import kotlin.reflect.KProperty1

@Serializable
class ElevatedBiosphere : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = if (count > 0) 32 + ((count-1) * 10) else 0
//    override val cost: CurrencyCost
//        get() = CurrencyCost.genericMechanicalParts(count, 2000).genericPlastic(count, 300)
    override val cost: CurrencyCost
        get() = CurrencyCost.NONE
    val positions: MutableList<@Serializable(PointSerializer::class) Point> = mutableListOf()
    @Transient var enabledPositions: MutableList<Point> = mutableListOf()

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
        positions += playerTarget.buildingPosition
    }

    override fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean {
        positions -= point.buildingPosition
        return true
    }

    override fun tick(data: BlockData) {
        enabledPositions = positions
        return
        val canHandleList = mutableListOf<Point>()
        for (pos in positions.sortedBy { it.y }) {
            if (data.lubricant >= (canHandleList.size + 1) * 40) {
                canHandleList += pos
            }
        }
        data.lubricant -= canHandleList.size * 40
        enabledPositions = canHandleList
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(player.data ?: return)
    }

    @BuildingCompanion(orderAfter = "first", category = SkyCategory::class)
    companion object ElevatedBiosphereCompanion : Building.BuildingCompanion {
        override val block: Block = Block.BEACON
        override val identifier: String = "sky:biosphere"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::elevatedBiospheres

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.BEACON) {
                itemName = ("<aqua>$SKY_SYMBOL Elevated Biosphere</aqua> <gray>-</gray>" +
                        "<white> $MECHANICAL_SYMBOL ${cost.mechanicalParts} $plasticColor$PLASTIC_SYMBOL ${cost.plastic}").asMini()
                lore {
                    +"<dark_gray>Allows claiming of sky blocks within a 50 block".asMini()
                    +"<dark_gray>radius on both the current and next level".asMini()
                    +"<gray>Consumes 40 $lubricant per research tick".asMini().noItalic()
                    +"<gray>Consumes 32 $disposableResources on the first construction".asMini().noItalic()
                    +("<gray>Consumes 10 $disposableResources on any further construction " +
                            "<dark_gray>(${if (count > 0) 32 + ((count-1) * 10) else 0})").asMini().noItalic()
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack = with(playerData.buildings.elevatedBiospheres) {
            getItem(cost, count)
        }

        override fun getResourceUse(currentDisposableResources: Int, count: Int): Int =
            currentDisposableResources + if (count == 0) 32 else 10
    }
}