package io.github.flyingpig525.building

import io.github.flyingpig525.MATTER_SYMBOL
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
class MatterContainer : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 2

    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.withY(40.0), block, false)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[4] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) {
        player.inventory[4] = Barrack.getItem(data)
    }

    companion object : Building.BuildingCompanion {
        override val block: Block = Block.LANTERN
        override val identifier: String = "matter:container"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.LANTERN) {
                itemName = "<green>$MATTER_SYMBOL Organic Matter Container <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<gray>Increases Max $organicMatter Storage".asMini().noItalic()
                    resourcesConsumed(2)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)

            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.containerCost, playerData.matterContainers.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2

        init {
            Building.BuildingCompanion.registry += this
        }
    }
}