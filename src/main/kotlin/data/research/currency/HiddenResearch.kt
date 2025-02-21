package io.github.flyingpig525.data.research.currency

import io.github.flyingpig525.data.research.upgrade.RefundResourcesOnDestruction
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import kotlinx.serialization.Serializable
import net.minestom.server.item.Material

@Serializable
class HiddenResearch : ResearchCurrency {
    override val symbol: String = ""
    override val color: String = ""
    // Makes this a hidden currency
    override val currencyLevel: Int = 0
    override val colorItem: Material = Material.AIR
    override var internalLevel: Int = 1
    override var count: Long = 0
    override val upgrades: List<ResearchUpgrade> = listOf(
        RefundResourcesOnDestruction()
    )
}