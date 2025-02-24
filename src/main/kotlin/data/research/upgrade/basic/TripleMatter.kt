package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
class TripleMatter : ResearchUpgrade() {
    override var maxLevel: Int = 3
    override val name: String = "Triple Organic Matter"
    override val cost: Long
        get() = (level * 400L) + 400L

    override fun onMatterBuildingTick(eventData: ActionData.MatterBuildingTick): ActionData.MatterBuildingTick? {
        if (level == 0) return null
        return eventData.apply {
            increase *= 3 * level
        }
    }

    override fun item(currency: ResearchCurrency): ItemStack {
        if ((currency as BasicResearch).noOp) return ItemStack.AIR
        return item(Material.OAK_LEAVES) {
            itemName = "<gold><bold>Triple Organic Matter Increase</bold> <gray>-<aqua> Level: $level/$maxLevel".asMini()
            lore {
                +"<dark_gray>Triples organic matter gain for each level".asMini().noItalic()
                +"<gold>Cost: $cost".asMini().noItalic()
            }
        }
    }
}