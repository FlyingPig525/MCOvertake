package io.github.flyingpig525.building.category

import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

object UndergroundCategory : BuildingCategory() {
    override val icon: ItemStack = item(Material.COBBLESTONE) {
        itemName = "Underground".asMini()

        lore {
            +"<gray>Category".asMini().noItalic()
        }
    }
}