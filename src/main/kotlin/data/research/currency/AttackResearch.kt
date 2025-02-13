package io.github.flyingpig525.data.research.currency

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.data.research.upgrade.*
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.Material

@Serializable
class AttackResearch : ResearchCurrency {
    @Transient override val symbol: String = Companion.symbol
    @Transient override val color: String = Companion.color
    @Transient override val currencyLevel: Int = 2
    @Transient override val colorItem: Material = Companion.colorItem
    @Required override var internalLevel: Int = 1
        set(value) {
            field = value
            upgrades.onEach { it.onCurrencyUpgrade(value) }
        }
    @Required override var count: Long = 0
    @Required override val upgrades: List<ResearchUpgrade> = listOf(
        QuickerCornering(),
        T2()
    )

    companion object {
        const val symbol = "[]"
        val color = NamedTextColor.RED.asHexString()
        val colorItem = Material.RED_WOOL
    }
}