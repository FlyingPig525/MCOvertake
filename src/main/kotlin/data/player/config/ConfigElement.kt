package io.github.flyingpig525.data.player.config

import io.github.flyingpig525.serialization.MaterialSerializer
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
data class ConfigElement(@Serializable(MaterialSerializer::class) val icon: Material, val name: String, var value: Boolean) {
    val iconWithValue: ItemStack
        get() {
        val color = if (value) "green" else "red"
        return getItem().withLore(
            listOf("<$color><bold>${value.toString().uppercase()}".asMini())
        )
    }
    fun getItem() = item(icon) { itemName = name.asMini() }
}