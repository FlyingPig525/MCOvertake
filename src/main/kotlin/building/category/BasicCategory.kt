package io.github.flyingpig525.building.category

import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

object BasicCategory : BuildingCategory() {
    override val icon: ItemStack = item(Material.OAK_LOG) {
        itemName = "Basic".asMini()
        lore {
            +"<gray>Category".asMini().noItalic()
        }
    }
}