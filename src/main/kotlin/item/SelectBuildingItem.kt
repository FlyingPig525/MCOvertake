package io.github.flyingpig525.item

import io.github.flyingpig525.data.Building
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.players
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
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

object SelectBuildingItem : Actionable {
    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.BRICK) {
            itemName = "<gray>Blueprint Constructor".asMini()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val inventory = Inventory(InventoryType.CHEST_3_ROW, "Select Blueprint")
        val playerData = players[event.player.uuid]!!


        inventory[2, 1] = Building.TrainingCamp.item(playerData.trainingCampCost)
        inventory[3, 1] = Building.Barrack.item(playerData.barracksCost)
        inventory[5, 1] = Building.MatterExtractor.item(playerData.extractorCost)
        inventory[6, 1] = Building.MatterContainer.item(playerData.containerCost)
        event.player.openInventory(inventory)

        val inventoryEventNode = EventNode.type("click", EventFilter.INVENTORY) { _, inv -> inventory == inv }
            .listen<InventoryClickEvent> { e ->
                val data = players[e.player.uuid]!!
                var close = true
                when(e.clickedItem) {
                    Building.TrainingCamp.item(playerData.trainingCampCost) -> {
                        data.trainingCamps.setBuildingItem(e.player.inventory, data.trainingCampCost)
                    }
                    Building.MatterExtractor.item(playerData.extractorCost) -> {
                        data.matterExtractors.setBuildingItem(e.player.inventory, data.extractorCost)
                    }
                    Building.MatterContainer.item(playerData.containerCost) -> {
                        data.matterExtractors.setBuildingItem(e.player.inventory, data.containerCost)
                    }
                    Building.Barrack.item(playerData.barracksCost) -> {
                        data.barracks.setBuildingItem(e.player.inventory, data.barracksCost)
                    }
                    else -> { close = false }
                }
                if (close) e.player.closeInventory()
            }

        GlobalEventHandler.addChild(inventoryEventNode)

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[4] = getItem(player.uuid)
    }
}