package io.github.flyingpig525.data.research.upgrade

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
class AdjacentWallBonusPercentageIncrease : ResearchUpgrade() {
    override var maxLevel: Int = 4
    override val name: String = "Fine Tuned Wall Structures"
    override val cost: Long get() {
        if (level <= 3) {
            return listOf(5000L, 20000L, 40000L, 60000L)[level]
        }
        return (60000L * (1.25.pow(level)) / 10).toLong() * 10L
    }

    override fun item(): ItemStack = researchItem(Material.NETHERITE_SCRAP, this) {
        lore {
            +"<dark_gray>Increases the exponential wall attack cost percentage".asMini().noItalic()
            +"<dark_gray>given from adjacent walls by 2% for each level".asMini().noItalic()
        }
    }

    override fun onPurchase(clickEvent: InventoryConditionResult, currency: ResearchCurrency, player: Player): PurchaseState {
        val ret = super.onPurchase(clickEvent, currency, player)
        if (ret is PurchaseState.Success) {
            (currency as BasicResearch).adjacentWallPercentageIncrease += 0.02
        }
        return ret
    }
}