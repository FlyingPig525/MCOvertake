package io.github.flyingpig525.item

import io.github.flyingpig525.BUILDING_SYMBOL
import io.github.flyingpig525.building.*
import io.github.flyingpig525.players
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.get
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

    init {
        Actionable.registry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.BRICK) {
            itemName = "<gold>$BUILDING_SYMBOL <bold>Blueprint Constructor</bold> $BUILDING_SYMBOL".asMini()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Select Blueprint")
        val playerData = players[event.player.uuid.toString()]!!
        val clearItem = item(Material.BARRIER) { itemName = "<red><bold>Clear Selected Item".asMini() }

        val blackItem = item(Material.BLACK_STAINED_GLASS_PANE) { itemName = "".asMini()}
        val yellowItem = item(Material.YELLOW_STAINED_GLASS_PANE) { itemName = "".asMini()}
        for (i in 0..8) {
            inventory[i, 0] = if (i % 2 == 0) blackItem else yellowItem
            inventory[i, 4] = if (i % 2 == 0) blackItem else yellowItem
        }
        inventory[0, 1] = yellowItem
        inventory[8, 1] = yellowItem
        inventory[0, 2] = blackItem
        inventory[8, 2] = blackItem
        inventory[0, 3] = yellowItem
        inventory[8, 3] = yellowItem

        for ((i, building) in Building.BuildingCompanion.registry.withIndex()) {
            val x = (i % 7) + 1
            val y = (i / 7) + 1
            inventory[x, y] = building.getItem(playerData)
        }
        inventory[4, 3] = clearItem
        event.player.openInventory(inventory)

        val inventoryEventNode = EventNode.type("select-building-inv", EventFilter.INVENTORY) { _, inv -> inventory == inv }
            .listen<InventoryClickEvent> { e ->
                val data = players[e.player.uuid.toString()]!!
                var close = true
                when(e.clickedItem) {
                    TrainingCamp.getItem(playerData) -> {
                        data.trainingCamps.select(e.player, playerData.trainingCampCost)
                    }
                    MatterExtractor.getItem(playerData) -> {
                        data.matterExtractors.select(e.player, playerData.extractorCost)
                    }
                    MatterContainer.getItem(playerData) -> {
                        data.matterContainers.select(e.player, playerData.containerCost)
                    }
                    Barrack.getItem(playerData) -> {
                        data.barracks.select(e.player, playerData.barracksCost)
                    }
                    clearItem -> {
                        setItemSlot(e.player)
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

    fun updatePlayerItem(player: Player) {
        val data = players[player.uuid.toString()]!!
        when(player.inventory[4].material()) {
            TrainingCamp.getItem(data).material() -> {
                data.trainingCamps.select(player, data.trainingCampCost)
            }
            MatterExtractor.getItem(data).material() -> {
                data.matterExtractors.select(player, data.extractorCost)
            }
            MatterContainer.getItem(data).material() -> {
                data.matterContainers.select(player, data.containerCost)
            }
            Barrack.getItem(data).material() -> {
                data.barracks.select(player, data.barracksCost)
            }

            else -> {}
        }
    }
}