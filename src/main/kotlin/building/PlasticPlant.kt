package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
import io.github.flyingpig525.data.player.PlayerData
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
class PlasticPlant : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 3 * count
    override val cost: Int
        get() = genericBuildingCost(count, 400)

    override fun place(playerTarget: Point, instance: Instance, data: PlayerData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun tick(data: PlayerData) {
        data.plastic += 20 * count
    }

    @BuildingCompanion("OilExtractor")
    companion object PlasticPlantCompanion : Building.BuildingCompanion, Validated {
        override var menuSlot: Int = 0
        override val block: Block = Block.CAMPFIRE
        override val identifier: String = "oil:plastic_plant"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::plasticPlants

        override fun getItem(cost: Int, count: Int): ItemStack = item(Material.CAMPFIRE) {
            itemName = "$plasticColor$PLASTIC_SYMBOL Plastic Plant <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
            lore {
                +"<dark_gray>Processes oil provided by Oil Extractors".asMini()
                +"<dark_gray>to create Plastic"
                +"<gray>Generates 20 $plastic".asMini().noItalic()
                +"<gray>Must be placed directly next to an Oil Extractor".asMini().noItalic()
                resourcesConsumed(3, count)
                amountOwned(count)
            }
            set(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: PlayerData): ItemStack =
            getItem(playerData.buildings.plasticPlants.cost, playerData.buildings.plasticPlants.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3

        override fun validate(instance: Instance, point: Point): Boolean {
            if (!point.isUnderground) return false
            var count = 0
            val extractors: MutableSet<Point> = mutableSetOf()
            val positions: MutableSet<Point> = mutableSetOf()
            point.repeatDirection { point, _ ->
                if (Building.getBuildingByBlock(instance.getBlock(point)) == OilExtractor && OilExtractor.validate(instance, point)) {
                    extractors += point
                    point.repeatDirection { point, _ ->
                        if (Building.getBuildingByBlock(instance.getBlock(point)) in OilExtractor.oilExtractorDependents
                            && point !in positions
                        ) {
                            count++
                            positions += point
                        }
                        false
                    }
                }
                false
            }
            return count < extractors.size
        }
    }
}