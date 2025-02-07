package io.github.flyingpig525.data.player.config

import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.entity.Player
import net.minestom.server.item.Material
import net.minestom.server.utils.time.Cooldown
import java.lang.reflect.Field
import java.time.Duration
import java.time.Instant

@Serializable
class PlayerConfig {
    // see comment in BlockConfig.kt
    @JvmField
    val claimParticles = ConfigElement(
        Material.GRASS_BLOCK,
        Material.DIRT,
        "Particles on Claim",
        true
    )

    fun map(): Map<String, Field> = this::class.java.declaredFields.associateBy { it.name }.filter {
        it.key != "Companion" && it.key != "onChangeFunctions"
    }

    companion object {
        /**
         * Key is the property name
         *
         * Lambda handles the change in value
         */
        val onChangeFunctions: MutableMap<String, (player: Player, element: ConfigElement) -> Unit> = mutableMapOf()
    }
}