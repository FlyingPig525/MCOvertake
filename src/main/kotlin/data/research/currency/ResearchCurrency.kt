package io.github.flyingpig525.data.research.currency

import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import net.minestom.server.item.Material

interface ResearchCurrency {
    val symbol: String
    val color: String
    val currencyLevel: Int
    val upgrades: List<ResearchUpgrade>
    val colorItem: Material
    var internalLevel: Int
    var count: Long

    fun upgradeByName(name: String) = upgrades.find { it.name == name }

    companion object {
        val currencies = mutableSetOf<ResearchCurrency>()
    }
}