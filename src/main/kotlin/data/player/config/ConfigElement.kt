package io.github.flyingpig525.data.player.config

import io.github.flyingpig525.serialization.MaterialSerializer
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
data class ConfigElement(
    @Serializable(MaterialSerializer::class) val trueIcon: Material,
    @Serializable(MaterialSerializer::class) val falseIcon: Material,
    val name: String,
    var value: Boolean,
    val trueText: String = "TRUE",
    val falseText: String = "FALSE",
    val hasOnChange: Boolean = false
) {
    val iconWithValue: ItemStack
        get() {
        val color = if (value) "green" else "red"
        return getItem().withLore(
            listOf("<$color><bold>${if (value) trueText else falseText}".asMini())
        )
    }
    fun getItem() = item(if (value) trueIcon else falseIcon) { itemName = name.asMini() }
}