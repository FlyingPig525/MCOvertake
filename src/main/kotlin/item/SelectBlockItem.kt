package io.github.flyingpig525.item

import io.github.flyingpig525.COLONY_SYMBOL
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.players
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*


object SelectBlockItem : Actionable {
    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.STRUCTURE_VOID) {
            itemName = "<green><bold>$COLONY_SYMBOL Select Block $COLONY_SYMBOL".asMini()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Select Block")

        inventory[4, 2] = item(Material.WARPED_HYPHAE) {
            itemName = "<gray>- <reset>Warped Hyphae <gray>-".asMini()
            lore {
                +"<gray>-| <green>Click to Select"
            }
        }
        event.player.openInventory(inventory)

        val inventoryEventNode = EventNode.type("select-block-inv", EventFilter.INVENTORY, {_, inv -> inventory == inv}).listen<InventoryClickEvent> { e ->
            players[e.player.uuid.toString()] = PlayerData(e.player.uuid.toString(), e.clickedItem.material().block()!!)
            e.player.closeInventory()
            for (i in 0..8) {
                e.player.inventory[i] = ItemStack.AIR
            }
            players[e.player.uuid.toString()]!!.updateBossBars()
            SelectBuildingItem.setItemSlot(e.player)
        }

        GlobalEventHandler.addChild(inventoryEventNode)

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[8] = getItem(player.uuid)
    }

    fun setAllSlots(player: Player) {
        for (i in 0..8) {
            player.inventory[i] = getItem(player.uuid)
        }
    }
}