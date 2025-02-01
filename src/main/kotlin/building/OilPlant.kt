package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.OilExtractor.OilExtractorCompanion
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.BuildingCompanion
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
class OilPlant : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 3 * count

    override fun place(playerTarget: Point, instance: Instance, data: PlayerData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(data)
    }

    override fun tick(data: PlayerData) {
        data.mechanicalParts += 20 * count
    }

    @BuildingCompanion("OilExtractor")
    companion object OilPlantCompanion : Building.BuildingCompanion, Validated {
        override var menuSlot: Int = 0
        override val block: Block = Block.CAMPFIRE
        override val identifier: String = "oil:plant"
        override val playerRef: KProperty1<PlayerData, Building> = PlayerData::oilPlants

        override fun getItem(cost: Int, count: Int): ItemStack = item(Material.CAMPFIRE) {
            itemName = "$oilColor$OIL_SYMBOL Oil Plant <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
            lore {
                +"<dark_gray>Processes oil provided by Oil Extractors".asMini()
                +"<dark_gray>to create Mechanical Parts"
                +"<gray>Generates 20 $mechanicalPart".asMini().noItalic()
                +"<gray>Must be placed directly next to an Oil Extractor".asMini().noItalic()
                resourcesConsumed(3, count)
                amountOwned(count)
            }
            set(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: PlayerData): ItemStack = getItem(playerData.oilPlantCost, playerData.oilPlants.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3

        override fun validate(instance: Instance, point: Point): Boolean {
            if (!point.isUnderground) return false
            val count = mutableListOf(2, 2, 2, 2)
            var i = 0
            point.repeatDirection { point, _ ->
                count[i] = 0
                if (Building.getBuildingByBlock(instance.getBlock(point)) == OilExtractor) {
                    point.repeatDirection { point, _ ->
                        if (Building.getBuildingByBlock(instance.getBlock(point)) == OilPlant) count[i]++
                        false
                    }
                }
                i++
                false
            }
            return count.any { it < 2 }
        }

    }
}