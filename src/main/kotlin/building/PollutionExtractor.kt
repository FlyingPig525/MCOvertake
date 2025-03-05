package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.ElevatedBiosphere.ElevatedBiosphereCompanion
import io.github.flyingpig525.building.category.SkyCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
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
class PollutionExtractor : Building() {
    override val resourceUse: Int
        get() = 6 * count
    override val cost: CurrencyCost
        get() = CurrencyCost
            .genericMechanicalParts(count, 1500)
            .genericPlastic(count, 250)
            .genericOrganicMatter(count, 900.0)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(player.data ?: return)
    }

    override fun tick(data: BlockData) {
        if (count == 0) return
        val pollution = data.buildings.sumOf { if (it.producesPollution) it.count else 0 }
        data.lubricant -= 20 * count
        data.organicMatter += 0.05 * pollution
    }

    @io.github.flyingpig525.ksp.BuildingCompanion(orderAfter = "ElevatedBiosphere", category = SkyCategory::class)
    companion object PollutionExtractorCompanion : BuildingCompanion, Validated {
        override val block: Block = Block.CALIBRATED_SCULK_SENSOR
        override val identifier: String = "matter:generator_3"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::pollutionExtractors

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.CALIBRATED_SCULK_SENSOR) {
                itemName = ("<green>$MATTER_SYMBOL Pollution Extractor</green> <gray>-</gray>" +
                        "<white> $MECHANICAL_SYMBOL ${cost.mechanicalParts} $plasticColor$PLASTIC_SYMBOL ${cost.plastic}" +
                        "<green> $MATTER_SYMBOL ${cost.organicMatter}").asMini()
                lore {
                    +"<dark_gray>Utilizes the carbon from pollution".asMini()
                    +"<dark_gray>and turns it into usable organic matter".asMini()
                    +"<gray>Consumes 20 $lubricant per research tick".asMini().noItalic()
                    +"<gray>Produces 0.05 $organicMatter per pollution-producing building every 30 ticks".asMini().noItalic()
                    resourcesConsumed(6, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack {
            return getItem(playerData.buildings.pollutionExtractors.cost, playerData.buildings.pollutionExtractors.count)
        }

        override fun getResourceUse(currentDisposableResources: Int, count: Int): Int = currentDisposableResources + 6

        override fun validate(instance: Instance, point: Point): Boolean {
            return point.isSky
        }
    }
}