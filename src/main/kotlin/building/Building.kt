package io.github.flyingpig525.building

import io.github.flyingpig525.building.Building.BuildingCompanion.Companion.registry
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
    fun select(player: Player, data: PlayerData)

    interface BuildingCompanion {
        val block: Block
        val identifier: String
        fun getItem(cost: Int, count: Int): ItemStack
        fun getItem(playerData: PlayerData): ItemStack

        fun getResourceUse(currentDisposableResources: Int): Int

        companion object {
            val registry: MutableList<BuildingCompanion> = mutableListOf()
        }
    }

    companion object {
        fun blockIsBuilding(block: Block): Boolean {
            for (entry in registry) {
                if (entry.block == block.defaultState()) return true
            }
            return false
        }

        fun getBuildingIdentifier(block: Block): String? {
            for (entry in registry) {
                if (entry.block.defaultState() == block.defaultState()) return entry.identifier
            }
            return null
        }
    }
}
