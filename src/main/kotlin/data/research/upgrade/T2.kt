package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
class T2 : ResearchUpgrade() {
    override val cost: Long = 0
    override var level: Int = 0
    override var maxLevel: Int = 6
    override val name: String = "Get Money!"
    override val requiredInternalLevel: Int = 1

    override fun item(): ItemStack = item(Material.GOLD_INGOT) {
        itemName = "<gold><bold>$name</bold> <gray>-<aqua> Level: $level/$maxLevel".asMini()
    }

    override fun onPurchase(clickEvent: InventoryConditionResult, currency: ResearchCurrency): PurchaseState {
        if (level >= maxLevel) return PurchaseState.MaxLevel(currency, this)
        currency.count += (100 * level) + 100
        level++
        return PurchaseState.Success
    }
}