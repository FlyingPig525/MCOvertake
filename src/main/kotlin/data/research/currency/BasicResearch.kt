package io.github.flyingpig525.data.research.currency

import io.github.flyingpig525.data.research.upgrade.*
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.Material

@Serializable
class BasicResearch : ResearchCurrency {
    @Transient override val symbol: String = Companion.symbol
    @Transient override val color: String = Companion.color
    @Transient override val currencyLevel: Int = 1
    @Transient override val colorItem: Material = Companion.colorItem
    @Required override var internalLevel: Int = 1
        set(value) {
            field = value
            upgrades.onEach { it.onCurrencyUpgrade(value) }
        }
    @Required override var count: Long = 0
    @Required override val upgrades: MutableList<ResearchUpgrade> = mutableListOf(
        AdjacentWallBonusPercentageIncrease(),
        AdjacentWallBonusPercentageDecrease(),
        TestUpgrade(),
        T2(),
        TripleMatter(),
        UpMax()
    )
    var adjacentWallPercentageDecrease = 0.0
    var adjacentWallPercentageIncrease = 0.0

    fun validateUpgrades() {
        val fresh = BasicResearch()
        val missing = mutableListOf<ResearchUpgrade>()
        for ((i, upgrade) in fresh.upgrades.withIndex()) {
            if (i >= upgrades.size) {
                missing += upgrade
                continue
            }
            if (upgrade.name != upgrades[i].name) {
                missing += fresh.upgrades[i]
            }
        }
        upgrades += missing
    }

    companion object {
        const val symbol = "\uD83E\uDDEA"
        val color = NamedTextColor.AQUA.asHexString()
        val colorItem = Material.CYAN_WOOL
    }
}