package io.github.flyingpig525.building

import io.github.flyingpig525.data.PlayerData
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack

interface Building {
    var count: Int
    val resourceUse: Int

    fun place(playerTarget: Point, instance: Instance)
    fun select(player: Player, cost: Int)
    interface BuildingCompanion {
        val block: Block
        fun getItem(cost: Int, count: Int): ItemStack
        fun getItem(playerData: PlayerData): ItemStack

        fun getResourceUse(count: Int): Int

        companion object {
            val registry: MutableList<BuildingCompanion> = mutableListOf()
        }
    }
}
