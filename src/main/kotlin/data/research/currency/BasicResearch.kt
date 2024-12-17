package io.github.flyingpig525.data.research.currency

import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import io.github.flyingpig525.data.research.upgrade.T2
import io.github.flyingpig525.data.research.upgrade.TestUpgrade
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.Material

@Serializable
class BasicResearch : ResearchCurrency {
    @Transient override val symbol: String = "🧪"
    @Transient override val color: String = NamedTextColor.AQUA.asHexString()
    @Transient override val currencyLevel: Int = 1
    @Transient override val colorItem: Material = Material.CYAN_WOOL
    @Required override var internalLevel: Int = 1
    @Required override var count: Long = 0
    @Required override val upgrades: List<ResearchUpgrade> = listOf(TestUpgrade(), T2())
}