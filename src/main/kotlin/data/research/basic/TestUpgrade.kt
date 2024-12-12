package io.github.flyingpig525.data.research.basic

import io.github.flyingpig525.data.research.ResearchUpgrade
import io.github.flyingpig525.data.research.action.ActionData
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class TestUpgrade : ResearchUpgrade("Test", 1) {
    init {
        level++
    }

    override val cost: Int
        get() = 1

    override val item: ItemStack
        get() = item(Material.GOLDEN_HOE) {
            itemName = "<gold><bold>Literally the best upgrade <gray>-<aqua> Level: $level".asMini()
            lore {
                +"<dark_gray>Removes claim cooldown and cost".asMini().noItalic()
                +"Cost: $cost".asMini().noItalic()
            }
        }

    override fun onClaimLand(eventData: ActionData.ClaimLand): ActionData.ClaimLand? {
        if (level == 0) return null
        return eventData.apply {
            claimCooldown = 0
            claimCost = 0
        }
    }
}