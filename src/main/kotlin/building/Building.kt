package io.github.flyingpig525.building

import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack

interface Building {
    var count: Int

    fun place(playerTarget: Point, instance: Instance)
    fun select(player: Player, cost: Int)
    interface BuildingCompanion {
        val block: Block
        fun getItem(cost: Int): ItemStack
    }
}
