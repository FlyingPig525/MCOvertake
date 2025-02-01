package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.anyOnline
import io.github.flyingpig525.data.research.action.ActionData
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import kotlin.math.pow
import kotlin.math.roundToLong

@Serializable
class OnlineAttacking : ResearchUpgrade() {
    override var maxLevel: Int = 5
    override val name: String = "Online Attacking"
    override val cost: Long get() = ((level + 1.0).pow(1.5) * 500.0).roundToLong()

    override fun item(): ItemStack = researchItem(Material.GOLDEN_SWORD, this) {
        lore {
            +"<dark_gray>Decreases attack cooldown by 5% for each level".asMini().noItalic()
            +"<dark_gray>while the targeted player (or any of their co-op members)".asMini().noItalic()
            +"<dark_gray>are online.".asMini().noItalic()
        }
    }

    override fun onAttack(eventData: ActionData.Attack): ActionData.Attack? {
        if (level < 1) return null
        val uuids = eventData.instance!!.gameInstance!!.uuidParentsInverse[eventData.targetData.uuid]!! + eventData.targetData.uuid
        if (!eventData.instance.anyOnline(*uuids.toTypedArray())) return null
        eventData.attackCooldown = Cooldown(Duration.ofMillis(
            (eventData.attackCooldown.duration.toMillis() * (1.0 - (0.05 * level))).toLong()
        ))
        return eventData
    }
}