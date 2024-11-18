package io.github.flyingpig525.wall

import io.github.flyingpig525.instance
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.Block.*

val WOODEN_FENCE_RANGE = 2..11
val BRICK_FENCE_RANGE = 12..12
val WALL_RANGE = 13..31
val GLASS_PANE_RANGE = 32..41

private val walls = mapOf(
    IRON_BARS to 1,
    BIRCH_FENCE to 2,
    BAMBOO_FENCE to 3,
    OAK_FENCE to 4,
    JUNGLE_FENCE to 5,
    SPRUCE_FENCE to 6,
    DARK_OAK_FENCE to 7,
    ACACIA_FENCE to 8,
    MANGROVE_FENCE to 9,
    CRIMSON_FENCE to 10,
    WARPED_FENCE to 11,
    NETHER_BRICK_FENCE to 12,
    SANDSTONE_WALL to 13,
    RED_SANDSTONE_WALL to 14,
    COBBLESTONE_WALL to 15,
    ANDESITE_WALL to 16,
    DIORITE_WALL to 17,
    GRANITE_WALL to 18,
    MUD_BRICK_WALL to 19,
    BRICK_WALL to 20,
    TUFF_WALL to 21,
    COBBLED_DEEPSLATE_WALL to 22,
    BLACKSTONE_WALL to 23,
    STONE_BRICK_WALL to 24,
    TUFF_BRICK_WALL to 25,
    DEEPSLATE_BRICK_WALL to 26,
    POLISHED_BLACKSTONE_BRICK_WALL to 27,
    NETHER_BRICK_WALL to 28,
    RED_NETHER_BRICK_WALL to 29,
    PRISMARINE_WALL to 30,
    END_STONE_BRICK_WALL to 31,
    WHITE_STAINED_GLASS_PANE to 32,
    LIGHT_GRAY_STAINED_GLASS_PANE to 33,
    YELLOW_STAINED_GLASS_PANE to 34,
    LIME_STAINED_GLASS_PANE to 35,
    GREEN_STAINED_GLASS_PANE to 36,
    PINK_STAINED_GLASS_PANE to 37,
    MAGENTA_STAINED_GLASS_PANE to 38,
    PURPLE_STAINED_GLASS_PANE to 39,
    RED_STAINED_GLASS_PANE to 40,
    BLACK_STAINED_GLASS_PANE to 41
)


fun getWallAttackCost(block: Block): Int? {
    val level = walls[block.defaultState()] ?: return null
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


val Block.wallLevel: Int?
    get() {
        if (blockIsWall(this)) return walls[this.defaultState()]!!
        return null
    }


fun blockIsWall(block: Block): Boolean = walls.containsKey(block.defaultState())

fun nextWall(level: Int): Block = walls.keys.elementAt(level)
fun lastWall(level: Int): Block = walls.keys.elementAt(level - 2)