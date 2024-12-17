package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.research.action.ActionData
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
class TestUpgrade : ResearchUpgrade() {

    override val name: String = "Test"
    override var maxLevel: Int = 1
    override val cost: Long
        get() = 600
    override var level: Int = 0
    override val requiredInternalLevel: Int = 1

    override fun item(): ItemStack = item(Material.GOLDEN_HOE) {
        itemName = "<gold><bold>Literally the best upgrade <gray>-<aqua> Level: $level/$maxLevel".asMini()
        lore {
            +"<dark_gray>Removes claim cooldown and cost".asMini().noItalic()
            +"<gold>Cost: $cost".asMini().noItalic()
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