package io.github.flyingpig525.data.block

import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.item.Material
import net.minestom.server.item.Material.*

interface CategoryBlock {
    val material: Material
}

val NATURAL_CATEGORY = item(OAK_LEAVES) { itemName = "<green><bold>Natural Blocks".asMini() }
val NETHER_CATEGORY = item(NETHERRACK) { itemName = "<red><bold>Nether Blocks".asMini() }
val UNDERGROUND_CATEGORY = item(COBBLESTONE) { itemName = "<gray><bold>Underground Blocks".asMini() }

enum class NaturalCategory(override val material: Material) : CategoryBlock {
    Dirt(DIRT),
    Coarse_Dirt(COARSE_DIRT),
    Oak_Log(OAK_LOG),
    Oak_Planks(OAK_PLANKS),
    Birch_Log(BIRCH_LOG),
    Birch_Planks(BIRCH_PLANKS),
    Spruce_Log(SPRUCE_LOG),
    Spruce_Planks(SPRUCE_PLANKS),
    Jungle_Log(JUNGLE_LOG),
    Jungle_Planks(JUNGLE_PLANKS),
    Acacia_Log(ACACIA_LOG),
    Acacia_Planks(ACACIA_PLANKS),
    Dark_Oak_Log(DARK_OAK_LOG),
    Dark_Oak_Planks(DARK_OAK_PLANKS),
    Mangrove_Log(MANGROVE_LOG),
    Mangrove_Planks(MANGROVE_PLANKS),
    Cherry_Log(CHERRY_LOG),
    Cherry_Planks(CHERRY_PLANKS),
    Podzol(PODZOL),
    Mycelium(MYCELIUM),
    Rooted_Dirt(ROOTED_DIRT),
    Mud(MUD),
    Clay(CLAY),
    Sand(SAND),
    Red_Sand(RED_SAND),
    Moss_Block(MOSS_BLOCK),
    Slime_Block(SLIME_BLOCK),
    White_Wool(WHITE_WOOL),
}

enum class UndergroundCategory(override val material: Material) : CategoryBlock {
    Stone(STONE),
    Cobblestone(COBBLESTONE),
    Deepslate(DEEPSLATE),
    Deepslate_Bricks(DEEPSLATE_BRICKS),
    Granite(GRANITE),
    Diorite(DIORITE),
    Andesite(ANDESITE),
    Calcite(CALCITE),
    Tuff(TUFF),
    Dripstone_Block(DRIPSTONE_BLOCK),
    Obsidian(OBSIDIAN),
    Crying_Obsidian(CRYING_OBSIDIAN),
    Coal_Ore(COAL_ORE),
    Coal_Block(COAL_BLOCK),
    Deepslate_Coal_Ore(DEEPSLATE_COAL_ORE),
    Iron_Ore(IRON_ORE),
    Iron_Block(IRON_BLOCK),
    Deepslate_Iron_Ore(DEEPSLATE_IRON_ORE),
    Copper_Ore(COPPER_ORE),
    Copper_Block(COPPER_BLOCK),
    Deepslate_Copper_Ore(DEEPSLATE_COPPER_ORE),
    Gold_Ore(GOLD_ORE),
    Gold_Block(GOLD_BLOCK),
    Deepslate_Gold_Ore(DEEPSLATE_GOLD_ORE),
    Redstone_Ore(REDSTONE_ORE),
    Redstone_Block(REDSTONE_BLOCK),
    Deepslate_Redstone_Ore(DEEPSLATE_REDSTONE_ORE),
    Emerald_Ore(EMERALD_ORE),
    Emerald_Block(EMERALD_BLOCK),
    Deepslate_Emerald_Ore(DEEPSLATE_EMERALD_ORE),
    Lapis_Ore(LAPIS_ORE),
    Lapis_Block(LAPIS_BLOCK),
    Deepslate_Lapis_Ore(DEEPSLATE_LAPIS_ORE),
    Diamond_Ore(DIAMOND_ORE),
    Diamond_Block(DIAMOND_BLOCK),
    Deepslate_Diamond_Block(DEEPSLATE_DIAMOND_ORE),
    Block_of_Amethyst(AMETHYST_BLOCK),
    Sculk(SCULK)
}

enum class NetherCategory(override val material: Material) : CategoryBlock {
    Netherrack(NETHERRACK),
    Crimson_Nylium(CRIMSON_NYLIUM),
    Warped_Nylium(WARPED_NYLIUM),
    Soul_Soil(SOUL_SOIL),
    Brown_Mushroom(BROWN_MUSHROOM_BLOCK),
    Red_Mushroom(RED_MUSHROOM_BLOCK),
    Nether_Wart(NETHER_WART_BLOCK),
    Warped_Wart(WARPED_WART_BLOCK),
    Warped_Hyphae(WARPED_HYPHAE),
    Crimson_Hyphae(CRIMSON_HYPHAE),
    Magma_Block(MAGMA_BLOCK),
    Blackstone(BLACKSTONE),
    Gilded_Blackstone(GILDED_BLACKSTONE),
    Polished_Blackstone(POLISHED_BLACKSTONE),
    Polished_Blackstone_Bricks(POLISHED_BLACKSTONE_BRICKS),
    Cracked_Polished_Blackstone_Bricks(CRACKED_POLISHED_BLACKSTONE_BRICKS),
    Basalt(BASALT),
    Smooth_Basalt(SMOOTH_BASALT),
    Nether_Gold_Ore(NETHER_GOLD_ORE),
    Nether_Quartz_Ore(NETHER_QUARTZ_ORE),
    Ancient_Debris(ANCIENT_DEBRIS),
}