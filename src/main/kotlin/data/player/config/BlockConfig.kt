package io.github.flyingpig525.data.player.config

import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.serialization.MaterialSerializer
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.utils.time.Cooldown
import java.lang.reflect.Field
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KProperty0

@Serializable

class BlockConfig {
    // for goofy aah reflection tactics (yippie), must be exposed in jvm as a public field
    @JvmField
    val doResearch = ConfigElement(
        trueIcon = Material.SCULK_SENSOR,
        falseIcon = Material.SCULK_SENSOR,
        name = "Enable Research Creation",
        value = true
    )
    @JvmField
    val doIntermediary = ConfigElement(
        trueIcon = Material.ANVIL,
        falseIcon = Material.BARRIER,
        name = "Enable Intermediary Resource Creation",
        value = true
    )
    @JvmField
    val sunOrMoon = ConfigElement(
        trueIcon = Material.PEARLESCENT_FROGLIGHT,
        falseIcon = Material.SCULK,
        name = "Use the Induced Glare Upgrade or the Suprise Attack Upgrade",
        value = true,
        trueText = "Induced Glare",
        falseText = "Suprise Attack",
        hasOnChange = true
    )
    fun map(): Map<String, Field> = this::class.java.declaredFields.associateBy { it.name }.filter { it.key != "Companion" }

    companion object {
        /**
         * Key is the property name
         *
         * Lambda handles the change in value
         */
        val onChangeFunctions: MutableMap<String, (player: Player, element: ConfigElement) -> Unit> = mutableMapOf(
            "sunOrMoon" to { player, el -> run {
                val data = player.data ?: return@run
                if (data.sunOrMoonChangeCooldown.isReady(Instant.now().toEpochMilli())) {
                    el.value = !el.value
                    data.sunOrMoonChangeCooldown = Cooldown(Duration.ofMinutes(player.gameInstance!!.instanceConfig.sunOrMoonCooldownLength))
                } else {
                    player.sendMessage("<red><bold>This setting is on cooldown and cannot be changed.".asMini())
                }
            }}
        )
    }
}