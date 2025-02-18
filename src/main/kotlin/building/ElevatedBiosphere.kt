package io.github.flyingpig525.building

import io.github.flyingpig525.MECHANICAL_SYMBOL
import io.github.flyingpig525.PLASTIC_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.category.SkyCategory
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.ksp.BuildingCompanion
import io.github.flyingpig525.ksp.PlayerBuildings
import io.github.flyingpig525.serialization.PointSerializer
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
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
    val positions: MutableSet<@Serializable(PointSerializer::class) Point> = mutableSetOf()
    override val resourceUse: Int get() = if (count > 0) 32 + ((count-1) * 10) else 0
    override val cost: CurrencyCost
        get() = CurrencyCost.genericMechanicalParts(count, 2000).genericPlastic(count, 300)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
        positions += playerTarget.buildingPosition
    }

    override fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean {
        positions -= point.buildingPosition
        return true
    }

    override fun select(player: Player) {
        TODO("Not yet implemented")
    }

    @BuildingCompanion(orderAfter = "first", category = SkyCategory::class)
    companion object ElevatedBiosphereCompanion : Building.BuildingCompanion {
        override val block: Block = Block.BEACON
        override val identifier: String = "sky:biosphere"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::elevatedBiospheres

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.BEACON) {
                itemName = ("<aqua>INSERT_SYMBOL Elevated Biosphere</aqua> <gray>-</gray>" +
                        "<white> $MECHANICAL_SYMBOL ${cost.mechanicalParts} $plasticColor$PLASTIC_SYMBOL ${cost.plastic}").asMini()
                lore {
                    +"<dark_gray>Allows claiming of sky blocks".asMini()
                    +"<dark_gray>within a 100 block radius".asMini()
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