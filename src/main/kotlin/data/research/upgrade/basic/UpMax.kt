package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.entity.Player
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
class UpMax : ResearchUpgrade() {
    override var maxLevel: Int = 2
    override val name: String = "Up Maxes"
    override val cost: Long = 0

    override fun item(currency: ResearchCurrency, gameInstance: GameInstance): ItemStack {
        if (!gameInstance.instanceConfig.opResearch) return ItemStack.AIR
        return item(Material.ANVIL) {
            itemName =
                "<gold><bold>Increase Maximums</bold> <gray>-<aqua> Level: $level/$maxLevel".asMini()
            lore {
                +"<dark_gray>Increases matter and power max".asMini().noItalic()
                +"<gold>Cost: $cost".asMini().noItalic()
            }
        }
    }

    override fun onPurchase(clickEvent: InventoryConditionResult, currency: ResearchCurrency, player: Player): PurchaseState {
        val ret = super.onPurchase(clickEvent, currency, player)
        if (ret.success) {
            player.data!!.buildings.barracks.count += 1000
            player.data!!.buildings.matterContainers.count += 1000
            player.data!!.blocks += 1000 * 5 * 50
        }
        return ret
    }
}