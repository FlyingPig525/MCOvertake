package io.github.flyingpig525.building

import io.github.flyingpig525.BUILDING_INVENTORY_SLOT
import io.github.flyingpig525.MECHANICAL_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.category.BasicCategory
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.data.research.currency.BasicResearch
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
class BasicResearchGenerator : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 3 * count
    override val cost get() = CurrencyCost.genericMechanicalParts(count, 100)
    override fun place(playerTarget: Point, instance: Instance, playerData: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun tick(data: BlockData) {
        if (!data.blockConfig.doResearch.value) return
        if (data.power >= 2 * count && data.mechanicalParts >= 3 * count) {
            data.power -= 2 * count
            data.mechanicalParts -= 3 * count
            data.research.basicResearch.count += count
        }
    }

    @BuildingCompanion(orderAfter = "MatterCompressionPlant", BasicCategory::class, "basicResearchStations")
    companion object BasicResearchGeneratorCompanion : Building.BuildingCompanion {
        override val block: Block = Block.SCULK_SENSOR
        override val identifier: String = "research:basic_research"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::basicResearchStations

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.SCULK_SENSOR) {
                itemName = "<${BasicResearch.color}>${BasicResearch.symbol} Basic Research Station <gray>-</gray><white> $MECHANICAL_SYMBOL ${cost.mechanicalParts}".asMini()
                lore {
                    +"<dark_gray>Employs scientists to aid development of new tech".asMini()
                    +"<dark_gray>using mechanical parts"
                    +"<gray>Consumes 2 $power and 3 $mechanicalPart to generate 1 basic research".asMini().noItalic()
                    resourcesConsumed(3, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack {
            return getItem(playerData.buildings.basicResearchStations.cost, playerData.buildings.basicResearchStations.count)
        }

        override fun getResourceUse(currentDisposableResources: Int, count: Int): Int = currentDisposableResources + 3
    }
}