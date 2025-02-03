package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
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
class LubricantProcessor : Building {
    override var count: Int = 0
    override val resourceUse: Int = 3
    override val cost: Int
        get() = genericBuildingCost(count, 400)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun tick(data: BlockData) {
        data.lubricant += 5 * count
    }

    @BuildingCompanion("PlasticPlant")
    companion object LubricantProcessorCompanion : Building.BuildingCompanion, Validated {
        override var menuSlot: Int = 0
        override val block: Block = Block.SOUL_CAMPFIRE
        override val identifier: String = "oil:lubricant_processor"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::lubricantProcessors

        override fun getItem(cost: Int, count: Int): ItemStack = item(Material.SOUL_CAMPFIRE) {
            itemName = "$lubricantColor$LUBRICANT_SYMBOL Lubricant Processor <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
            lore {
                +"<dark_gray>Processes oil provided by Oil Extractors".asMini()
                +"<dark_gray>to create Lubricant"
                +"<gray>Generates 5 $lubricant".asMini().noItalic()
                +"<gray>Must be placed directly next to an Oil Extractor".asMini().noItalic()
                resourcesConsumed(3, count)
                amountOwned(count)
            }
            set(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: BlockData): ItemStack =
            getItem(playerData.buildings.lubricantProcessors.cost, playerData.buildings.lubricantProcessors.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3

        override fun validate(instance: Instance, point: Point): Boolean {
            if (!point.isUnderground) return false
            val count = mutableListOf(2, 2, 2, 2)
            var i = 0
            point.repeatDirection { point, _ ->
                count[i] = 0
                if (Building.getBuildingByBlock(instance.getBlock(point)) == OilExtractor && OilExtractor.validate(instance, point)) {
                    point.repeatDirection { point, _ ->
                        if (Building.getBuildingByBlock(instance.getBlock(point)) in OilExtractor.oilExtractorDependents) count[i]++
                        false
                    }
                } else count[i] = 2
                i++
                false
            }
            return count.any { it < 2 }
        }

    }
}