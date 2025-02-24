package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.playerPosition
import io.github.flyingpig525.repeatAdjacent
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import kotlin.math.pow

@Serializable
class QuickerCornering : ResearchUpgrade() {
    override var maxLevel: Int = 3
    override val name: String = "Quicker Cornering"
    override val cost: Long get() = 100 * 12.0.pow(level).toLong()

    override fun item(currency: ResearchCurrency): ItemStack {
        return researchItem(Material.COMPASS, this) {
            lore {
                +"<dark_gray>Decreases attack cooldown by 1.5%(+0.5% after level 1) for each block".asMini().noItalic()
                +"<dark_gray>you have adjacent to the block you are attacking.".asMini().noItalic()
                +"<dark_gray>Only activates after you have 3 adjacent blocks.".asMini().noItalic()
            }
        }
    }

    override fun onAttack(eventData: ActionData.Attack): ActionData.Attack? {
        if (level < 1) return null
        val target = eventData.player?.getTrueTarget(20) ?: return null
        var blocks = 0
        target.playerPosition.repeatAdjacent {
            if (eventData.instance!!.getBlock(it).defaultState() == eventData.playerData.block) blocks++
        }
        if (blocks >= 3) {
            var cooldownMillis = eventData.attackCooldown.duration.toMillis()
            cooldownMillis = (cooldownMillis.toDouble() * (100.0 - 1.5 - (0.5 * (level - 1)))).toLong()
            eventData.attackCooldown = Cooldown(Duration.ofMillis(cooldownMillis))
            return eventData
        }
        return null
    }
}