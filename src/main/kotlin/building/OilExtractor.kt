package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
import io.github.flyingpig525.building.category.UndergroundCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.ksp.BuildingCompanion
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import kotlin.reflect.KProperty1

@Serializable
class OilExtractor : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 3 * count
    override val cost: Int
        get() = genericBuildingCost(count, 300)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean {
        return !point.buildingPosition.repeatDirection { point, dir ->
            Building.getBuildingByBlock(instance.getBlock(point)) == PlasticPlant
        }
    }

    @BuildingCompanion("OilPatch", UndergroundCategory::class)
    companion object OilExtractorCompanion : Building.BuildingCompanion, Validated {
        override val block: Block = Block.BLACK_CANDLE.withProperty("candles", "4")
        override val identifier: String = "oil:extractor"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::oilExtractors
        val oilExtractorDependents: Set<Building.BuildingCompanion> = setOf(PlasticPlant, LubricantProcessor)

        override fun getItem(cost: Int, count: Int): ItemStack = item(Material.BLACK_CANDLE) {
            itemName = "$oilColor$OIL_SYMBOL Oil Extractor <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
            lore {
                +"<dark_gray>Extracts oil from surface rock".asMini()
                +"<gray>Supplies enough oil to support 2".asMini().noItalic()
                +"<gray>dependents".asMini().noItalic()
                +"<gray>Must be placed directly next to an Oil Patch".asMini().noItalic()
                resourcesConsumed(3, count)
                amountOwned(count)
            }
            set(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: BlockData): ItemStack =
            getItem(playerData.buildings.oilExtractors.cost, playerData.buildings.oilExtractors.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3

        override fun validate(instance: Instance, point: Point): Boolean {
            if (!point.isUnderground) return false
            return point.buildingPosition.repeatDirection { point, _ ->
                val block = instance.getBlock(point)
                if (Building.getBuildingByBlock(block) == OilPatch) {
                    true
                } else false
            }
        }
    }
}