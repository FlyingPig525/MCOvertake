package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant

@Serializable
class MessageWhenAttacked : ResearchUpgrade(level = 1) {
    override var maxLevel: Int = 1
    override val name: String = "Message When Attacked"
    override val cost: Long = 0L
    @Transient private var cooldown: Cooldown = Cooldown(Duration.ZERO)

    override fun item(currency: ResearchCurrency, gameInstance: GameInstance): ItemStack = ItemStack.AIR

    override fun onAttacked(eventData: ActionData.Attacked): ActionData.Attacked? {
        if (!cooldown.isReady(Instant.now().toEpochMilli())) return null
        eventData.playerData.alertLocation = eventData.location
            ?.add(2.0, 4.0, 0.0)
            ?.withLookAt(eventData.location!!)
        eventData.playerData.onPlayers {
            val message = "<red><bold>You have been attacked by ${eventData.attackerPlayer!!.username}!".asMini()
                .appendNewline()
                .append("<red><bold>Click ".asMini()
                    .append("<reset><light_purple>[here]".asMini().clickEvent(ClickEvent.runCommand("/tpAlert")))
                    .append("<reset><red><bold> to teleport there".asMini())
                )
            it.sendMessage(message)
        }
        cooldown = Cooldown(Duration.ofSeconds(15))
        eventData.instance?.scheduler()?.scheduleTask({
            eventData.playerData.alertLocation = null
            TaskSchedule.stop()
        }, TaskSchedule.minutes(1))
        return null
    }
}