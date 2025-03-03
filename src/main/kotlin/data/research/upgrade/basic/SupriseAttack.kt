package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
class SupriseAttack : ResearchUpgrade() {
    override var maxLevel: Int = 1
    override val name: String = "Suprise Attack"
    override val cost: Long = 1000

    override fun item(currency: ResearchCurrency, gameInstance: GameInstance): ItemStack = researchItem(Material.SCULK, this) {
        lore {
            +"<dark_gray>Reduces attack cost by 5% when attacking".asMini().noItalic()
            +"<dark_gray>under moonlight".asMini().noItalic()
        }
    }

    override fun onAttack(eventData: ActionData.Attack): ActionData.Attack? {
        if (level < 1) return null
        if (eventData.player?.data?.blockConfig?.sunOrMoon?.value != false) return null
        if ((eventData.instance!!.time % 24000L) !in 13000L..23000L) return null
        return eventData.apply { attackCost = (attackCost.toDouble() * 0.95).toInt() }
    }
}