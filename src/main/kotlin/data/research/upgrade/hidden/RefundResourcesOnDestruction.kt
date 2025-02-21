package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.research.action.ActionData
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.text.Component
import net.minestom.server.item.ItemStack
import kotlin.math.absoluteValue

@Serializable
class RefundResourcesOnDestruction : ResearchUpgrade(level = 1) {
    override var maxLevel: Int = 1
    override val name: String = "Refund Resources on Destruction"
    override val cost: Long = 0

    override fun item(): ItemStack {
        TODO("This function should not be accessible!")
    }

    override fun onDestroyBuilding(eventData: ActionData.DestroyBuilding): ActionData.DestroyBuilding? {
        val refundAmount = eventData.building!!.cost.modify { it * -0.5 }
        refundAmount.apply(eventData.playerData)
        refundAmount.forEach { name, value ->
            if (value == 0.0) return@forEach
            eventData.player?.sendMessage(name.appendSpace().append("+${value.absoluteValue}".asMini()))
        }
        return null
    }
}