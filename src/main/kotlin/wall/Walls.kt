package io.github.flyingpig525.wall

import io.github.flyingpig525.instance
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.block.Block

private val walls = mapOf(
    Block.IRON_BARS to 1,
    Block.BIRCH_FENCE to 2,
    Block.OAK_FENCE to 3
)

fun getWallAttackCost(block: Block): Int? {
    val level = walls[block] ?: return null
    return level * 2
}

fun getWallAttackCost(point: Point): Int? {
    val block = instance.getBlock(point)
    if (walls.containsKey(block)) {
        return getWallAttackCost(block)!!
    }
    val above = instance.getBlock(point.add(0.0, 1.0, 0.0))
    if (walls.containsKey(above)) {
        return getWallAttackCost(block)!!
    }
    return null
}

fun getWallUpgradeCost(wall: Block): Int? {
    if (blockIsWall(wall.defaultState())) {
        val level = walls[wall.defaultState()]!! + 1
        return level * 5 + 10
    }
    return null
}

fun blockIsWall(block: Block): Boolean = walls.containsKey(block.defaultState())