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
class ElevatedBiosphere : Building() {
    override val resourceUse: Int get() = if (count > 0) 32 + ((count-1) * 10) else 0
    override val cost: CurrencyCost
        get() = CurrencyCost.genericMechanicalParts(count, 2000).genericPlastic(count, 850)
    override val itemGetter: (cost: CurrencyCost, count: Int) -> ItemStack
        get() = ::getItem

    val positions: MutableList<@Serializable(PointSerializer::class) Point> = mutableListOf()
    @Transient var enabledPositions: MutableList<Point> = mutableListOf()

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
        positions += playerTarget.buildingPosition
        enabledPositions(data)
    }

    private fun enabledPositions(data: BlockData) {
        val canHandleList = mutableListOf<Point>()
        for (pos in positions.sortedBy { it.y }) {
            if (data.lubricant >= (canHandleList.size + 1) * 40) {
                canHandleList += pos
            }
        }
        enabledPositions = canHandleList
    }

    override fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean {
        positions -= point.buildingPosition
        return true
    }

    override fun tick(data: BlockData) {
        enabledPositions(data)
        data.lubricant -= enabledPositions.size * 40
    }

    @io.github.flyingpig525.ksp.BuildingCompanion(orderAfter = "first", category = SkyCategory::class)
    companion object ElevatedBiosphereCompanion : Building.BuildingCompanion, Validated {
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
                    +"<dark_gray>If production cannot handle the total amount of elevated".asMini()
                    +"<dark_gray>biospheres you have, they will be disabled in the opposite".asMini()
                    +"<dark_gray>order you placed them in".asMini()
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

        override fun validate(instance: Instance, point: Point): Boolean = !point.isUnderground
    }
}