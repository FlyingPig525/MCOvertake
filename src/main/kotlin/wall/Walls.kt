package io.github.flyingpig525.wall

import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.Block.*
import java.util.HashMap

// These values will not be manually updated, set in map builder
var WOODEN_FENCE_RANGE = 2..12
    private set
var BRICK_FENCE_RANGE = 13..13
    private set
var WALL_RANGE = 14..32
    private set
var GLASS_PANE_RANGE = 33..42
    private set

private val wallBlocks = listOf(
    IRON_BARS,
    PALE_OAK_FENCE,
    BIRCH_FENCE,
    BAMBOO_FENCE,
    OAK_FENCE,
    JUNGLE_FENCE,
    SPRUCE_FENCE,
    DARK_OAK_FENCE,
    ACACIA_FENCE,
    MANGROVE_FENCE,
    CRIMSON_FENCE,
    WARPED_FENCE,
    NETHER_BRICK_FENCE,
    SANDSTONE_WALL,
    RED_SANDSTONE_WALL,
    COBBLESTONE_WALL,
    ANDESITE_WALL,
    DIORITE_WALL,
    GRANITE_WALL,
    MUD_BRICK_WALL,
    BRICK_WALL,
    RESIN_BRICK_WALL,
    TUFF_WALL,
    COBBLED_DEEPSLATE_WALL,
    BLACKSTONE_WALL,
    STONE_BRICK_WALL,
    TUFF_BRICK_WALL,
    DEEPSLATE_BRICK_WALL,
    POLISHED_BLACKSTONE_BRICK_WALL,
    NETHER_BRICK_WALL,
    RED_NETHER_BRICK_WALL,
    PRISMARINE_WALL,
    END_STONE_BRICK_WALL,
    WHITE_STAINED_GLASS_PANE,
    LIGHT_GRAY_STAINED_GLASS_PANE,
    YELLOW_STAINED_GLASS_PANE,
    LIME_STAINED_GLASS_PANE,
    GREEN_STAINED_GLASS_PANE,
    PINK_STAINED_GLASS_PANE,
    MAGENTA_STAINED_GLASS_PANE,
    PURPLE_STAINED_GLASS_PANE,
    RED_STAINED_GLASS_PANE,
    BLACK_STAINED_GLASS_PANE
)

private val walls = buildMap<Block, Int> {
    var woodenEnd = 0
    var brickFence = 0
    var wallStart = 0
    var wallEnd = 0
    var paneStart = 0
    for ((i, block) in wallBlocks.withIndex()) {
        put(block, i + 1)
        when (block) {
            NETHER_BRICK_FENCE -> {
                woodenEnd = i
                brickFence = i + 1
            }
            SANDSTONE_WALL -> {
                wallStart = i + 1
            }
            WHITE_STAINED_GLASS_PANE -> {
                wallEnd = i
                paneStart = i + 1
            }
            else -> {}
        }
    }
    WOODEN_FENCE_RANGE = 1..woodenEnd
    BRICK_FENCE_RANGE = brickFence..brickFence
    WALL_RANGE = wallStart..wallEnd
    GLASS_PANE_RANGE = paneStart..wallBlocks.size
}


fun getWallAttackCost(block: Block): Int? {
    val level = walls[block.defaultState()] ?: return null
    return level * 2
}

fun getWallAttackCost(level: Int): Int = level * 2

fun getWallUpgradeCost(wall: Block): Int? {
    if (blockIsWall(wall.defaultState())) {
        val level = walls[wall.defaultState()]!! + 1
        return level * 5 + 10
    }
    return null
}


val Block.wallLevel: Int
    get() {
        return walls[this.defaultState()] ?: 0
    }


fun blockIsWall(block: Block): Boolean = walls.containsKey(block.defaultState())

fun nextWall(level: Int): Block = walls.keys.elementAt(level)
fun lastWall(level: Int): Block = walls.keys.elementAt(level - 2)