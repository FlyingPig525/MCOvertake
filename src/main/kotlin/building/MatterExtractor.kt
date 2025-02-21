package io.github.flyingpig525.building

import io.github.flyingpig525.BUILDING_INVENTORY_SLOT
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.category.BasicCategory
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.data.research.action.ActionData
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
class MatterExtractor : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 3
    override val cost get() = CurrencyCost.genericOrganicMatter(count, 25.0)

    override fun place(playerTarget: Point, instance: Instance, playerData: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun tick(data: BlockData) {
        val increase = count * 0.5 + 0.5
        var action = ActionData.MatterBuildingTick(data).apply {
            this.increase = increase
        }
        action = data.research.onMatterBuildingTick(action)
        data.organicMatter += action.increase
    }

    @BuildingCompanion("Barrack", BasicCategory::class)
    companion object MatterExtractorCompanion : Building.BuildingCompanion {
        override val block: Block = Block.BREWING_STAND
        override val identifier: String = "matter:generator"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::matterExtractors

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.BREWING_STAND) {
                itemName = "<green>$MATTER_SYMBOL Organic Matter Extractor <gray>-<green> $MATTER_SYMBOL ${cost.organicMatter}".asMini()
                lore {
                    +"<dark_gray>Digs deep into the ground to extract organic materials".asMini()
                    +"<gray>Generates 0.5 $organicMatter every 30 ticks".asMini().noItalic()
                    resourcesConsumed(3, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack {
            return getItem(playerData.buildings.matterExtractors.cost, playerData.buildings.matterExtractors.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3

        override fun shouldCallItemUse(): Boolean = true
    }
}