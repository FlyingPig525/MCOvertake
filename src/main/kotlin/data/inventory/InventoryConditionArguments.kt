package io.github.flyingpig525.data.inventory

import net.minestom.server.entity.Player
import net.minestom.server.inventory.AbstractInventory
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.inventory.condition.InventoryConditionResult

data class InventoryConditionArguments(val player: Player, val slot: Int, val clickType: ClickType, val res: InventoryConditionResult) {
    companion object {
        fun AbstractInventory.addInventoryCondition(cond: (InventoryConditionArguments) -> Unit) {
            addInventoryCondition { a, b, c, d ->
                cond(InventoryConditionArguments(a, b, c, d))
            }
        }
    }
}
