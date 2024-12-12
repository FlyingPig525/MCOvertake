package io.github.flyingpig525.data.research.basic

import io.github.flyingpig525.data.research.ResearchCurrency
import io.github.flyingpig525.data.research.ResearchUpgrade
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.Material

@Serializable
class BasicResearch : ResearchCurrency {
    override val symbol: String = "🧪"
    @OptIn(ExperimentalStdlibApi::class)
    override val color: String = NamedTextColor.AQUA.value().toHexString()
    override val currencyLevel: Int = 1
    override val colorItem: Material = Material.CYAN_WOOL
    override var internalLevel: Int = 1
    override var count: Int = 0
    override val upgrades: List<ResearchUpgrade> = buildList {
        this += TestUpgrade()
    }
}