package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.TrainingCamp.TrainingCampCompanion
import io.github.flyingpig525.building.category.UndergroundCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
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
class ArmsManufacturer : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 5 * count
    override val cost: CurrencyCost get() = CurrencyCost.genericMechanicalParts(count, 75).genericPlastic(count, 50)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun tick(data: BlockData) {
        data.power += 1.5 * count
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(player.data ?: return)
    }

    @BuildingCompanion(orderAfter = "LubricantProcessor", category = UndergroundCategory::class)
    companion object ArmsManufacturerCompanion : Building.BuildingCompanion {
        override val block: Block = Block.SEA_PICKLE.withProperty("pickles", "2")
        override val identifier: String = "power:generator_2"

        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::armsManufacturers

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.SEA_PICKLE) {
                itemName = ("<red>$POWER_SYMBOL Arms Manufacturer</red> <gray>-</gray>" +
                        "<white> $MECHANICAL_SYMBOL ${cost.mechanicalParts} $plasticColor$PLASTIC_SYMBOL ${cost.plastic}").asMini()
                lore {
                    +"<dark_gray>Manufactures essential equipment".asMini()
                    +"<dark_gray>for invading land".asMini()
                    +"<gray>Generates 1.5 $power every 70 ticks".asMini().noItalic()
                    resourcesConsumed(5, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack = getItem(playerData.buildings.armsManufacturers.cost, playerData.buildings.armsManufacturers.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 5
    }
}