package io.github.flyingpig525.data.player.config

import io.github.flyingpig525.serialization.MaterialSerializer
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.lang.reflect.Field
import kotlin.reflect.KProperty0

@Serializable

class BlockConfig {
    // for goofy aah reflection tactics (yippie), must be exposed in jvm as a public field
    @JvmField
    val doResearch = ConfigElement(
        Material.SCULK_SENSOR,
        Material.SCULK_SENSOR,
        "Enable Research Creation",
        true
    )
    @JvmField
    val doIntermediary = ConfigElement(
        Material.ANVIL,
        Material.BARRIER,
        "Enable Intermediary Resource Creation",
        true
    )
    @JvmField
    val sunOrMoon = ConfigElement(
        Material.PEARLESCENT_FROGLIGHT,
        Material.SCULK,
        "Use the Induced Glare Upgrade or the Suprise Attack Upgrade",
        true,
        "Induced Glare",
        "Suprise Attack"
    )
    fun map(): Map<String, Field> = this::class.java.declaredFields.associateBy { it.name }

}