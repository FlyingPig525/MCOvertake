package io.github.flyingpig525.building

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.PlayerData
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

@Serializable
class MatterExtractor : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 3

    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.buildingPosition, block, false)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[4] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) {
        player.inventory[4] = getItem(data)
    }

    override fun tick(data: PlayerData) {
        data.organicMatter = ((count * 0.5 + 0.5) + data.organicMatter)
    }

    companion object : Building.BuildingCompanion {
        override val block: Block = Block.BREWING_STAND
        override val identifier: String = "matter:generator"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.BREWING_STAND) {
                itemName = "<green>$MATTER_SYMBOL Organic Matter Extractor <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<gray>Generates 0.5 $organicMatter".asMini().noItalic()
                    resourcesConsumed(3)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.extractorCost, playerData.matterExtractors.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3

        init {
            Building.BuildingCompanion.registry += this
        }
    }
}