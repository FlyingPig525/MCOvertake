package io.github.flyingpig525.building

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.data.PlayerData
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
class MatterContainer : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = getResourceUse(count)

    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.add(0.0, 1.0, 0.0), block, false)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[4] = getItem(cost, count)
    }

    companion object : Building.BuildingCompanion {
        override val block: Block = Block.LANTERN

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.LANTERN) {
                itemName = "<green>$MATTER_SYMBOL Organic Matter Container <gray>-<green> $MATTER_SYMBOL $cost".asMini()
            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.containerCost, playerData.matterContainers.count)
        }

        override fun getResourceUse(count: Int): Int = count * 2

        init {
            Building.BuildingCompanion.registry += this
        }
    }
}