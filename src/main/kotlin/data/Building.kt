package io.github.flyingpig525.data

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

sealed class Building(val item: (cost: Int) -> ItemStack) {
    val block: Block get() = item(0).material().block()!!
    var count: Int = 0

    fun setBuildingItem(inventory: PlayerInventory, cost: Int) {
        inventory[4] = item(cost)
    }

    fun place(playerTarget: Point, instance: Instance) {
        val prop = block.withProperty("face", "floor")
        instance.setBlock(playerTarget.add(0.0, 1.0, 0.0), prop, false)
        count++
    }

    class TrainingCamp : Building({ cost ->
        item(Material.POLISHED_BLACKSTONE_BUTTON) {
            itemName = "<red>$POWER_SYMBOL Training Camp</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
        }
    }) {
        companion object {
            val item: (cost: Int) -> ItemStack = { cost ->
                item(Material.POLISHED_BLACKSTONE_BUTTON) {
                    itemName =
                        "<red>$POWER_SYMBOL Training Camp</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
                }
            }
        }
    }

    class Barrack : Building({ cost ->
        item(Material.SOUL_LANTERN) {
            itemName = "<red>$POWER_SYMBOL Barracks</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
        }
    }) {
        companion object {
            val item: (cost: Int) -> ItemStack = { cost ->
                item(Material.SOUL_LANTERN) {
                    itemName = "<red>$POWER_SYMBOL Barracks</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
                }
            }
        }
    }

    class MatterExtractor : Building({ cost ->
        item(Material.BREWING_STAND) {
            itemName = "<green>$MATTER_SYMBOL Organic Matter Extractor <gray>-<green> $MATTER_SYMBOL $cost".asMini()
        }
    }) {
        companion object {
            val item: (cost: Int) -> ItemStack = { cost ->
                item(Material.BREWING_STAND) {
                    itemName = "<green>$MATTER_SYMBOL Organic Matter Extractor <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                }
            }
        }
    }

    class MatterContainer : Building({ cost ->
        item(Material.LANTERN) {
            itemName = "<green>$MATTER_SYMBOL Organic Matter Container <gray>-<green> $MATTER_SYMBOL $cost".asMini()
        }
    }) {
        companion object {
            val item: (cost: Int) -> ItemStack = { cost ->
                item(Material.LANTERN) {
                    itemName = "<green>$MATTER_SYMBOL Organic Matter Container <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                }
            }
        }
    }
}