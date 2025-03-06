package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.entity.Player
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import kotlin.math.pow

@Serializable
class AdjacentWallBonusPercentageDecrease : ResearchUpgrade() {
    override var maxLevel: Int = 4
    override val name: String = "Wall Fucker Upper"
    override val cost: Long get() {
        if (level <= 3) {
            return listOf(5500L, 21000L, 42000L, 63500L)[level]
        }
        return (63500L * (1.25.pow(level)) / 10).toLong() * 10L
    }

    override fun item(currency: ResearchCurrency, gameInstance: GameInstance): ItemStack = researchItem(Material.SCRAPE_POTTERY_SHERD, this) {
        lore {
            +"<dark_gray>Decreases the exponential wall attack cost percentage".asMini().noItalic()
            +"<dark_gray>given from adjacent walls when attacking players by 2% for each level".asMini().noItalic()
        }
    }

    override fun onPurchase(clickEvent: InventoryConditionResult, currency: ResearchCurrency, player: Player): PurchaseState {
        val ret = super.onPurchase(clickEvent, currency, player)
        if (ret is PurchaseState.Success) {
            (currency as BasicResearch).adjacentWallPercentageDecrease += 0.02
        }
        return ret
    }
}