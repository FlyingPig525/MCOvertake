package io.github.flyingpig525.data.player.config

import io.github.flyingpig525.data.inventory.InventoryConditionArguments
import io.github.flyingpig525.serialization.MaterialSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.minestom.server.item.ItemStack
import kotlin.reflect.KClass
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.Material
import kotlin.reflect.KProperty0

@Serializable
class PlayerConfig {
    @Required
    val claimParticles = ConfigElement(
        Material.GRASS_BLOCK,
        "Particles on Claim",
        true
    )

    fun map(): Map<String, KProperty0<ConfigElement>> = mapOf(::claimParticles.name to ::claimParticles)

    @Serializable
    data class ConfigElement(@Serializable(MaterialSerializer::class) val icon: Material, val name: String, var value: Boolean) {
        val iconWithValue: ItemStack get() {
            val color = if (value) "green" else "red"
            return getItem().withLore(
                listOf("<$color><bold>${value.toString().uppercase()}".asMini())
            )
        }
        fun getItem() = item(icon) { itemName = name.asMini() }
    }
}
