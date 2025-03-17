package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.organicMatter
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.wall.getWallUpgradeCost
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.item.ItemStack
import kotlin.math.absoluteValue

@Serializable
class RefundResourcesOnDestruction : ResearchUpgrade(level = 1) {
    override var maxLevel: Int = 1
    override val name: String = "Refund Resources on Destruction"
    override val cost: Long = 0

    override fun item(currency: ResearchCurrency, gameInstance: GameInstance): ItemStack {
        TODO("This function should not be accessible!")
    }

    override fun onDestroyBuilding(eventData: ActionData.DestroyBuilding): ActionData.DestroyBuilding? {
        if (eventData.building != null) {
            val refundAmount = eventData.building!!.cost.modify { it * -0.5 }
            refundAmount.apply(eventData.playerData)
            refundAmount.forEach { name, value ->
                if (value == 0.0) return@forEach
                eventData.player?.sendMessage(name.appendSpace().append("+${value.absoluteValue}".asMini()))
            }
        } else if (eventData.wallLevel != 0) {
            val refundAmount = getWallUpgradeCost(eventData.wallLevel)
            CurrencyCost(organicMatter = refundAmount.toDouble() * -0.5).apply(eventData.playerData)
            eventData.player?.sendMessage(organicMatter.asMini().appendSpace().append("+$refundAmount".asMini()))
        }
        return null
    }
}