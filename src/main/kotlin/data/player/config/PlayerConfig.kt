package io.github.flyingpig525.data.player.config

import kotlinx.serialization.Serializable
import net.minestom.server.entity.Player
import net.minestom.server.item.Material
import java.lang.reflect.Field

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