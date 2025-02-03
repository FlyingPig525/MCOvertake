package io.github.flyingpig525.item

import io.github.flyingpig525.BUILDING_SYMBOL
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.data
import io.github.flyingpig525.instances
import io.github.flyingpig525.ksp.Item
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.get
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*

@Item(persistent = true)
object SelectBuildingItem : Actionable {

    override val identifier: String = "item:select_building"
    override val itemMaterial: Material = Material.BRICK


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<gold>$BUILDING_SYMBOL <bold>Blueprint Constructor</bold> $BUILDING_SYMBOL".asMini()
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Select Blueprint")
        val playerData = event.player.data!!
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

        for ((i, building) in Building.BuildingCompanion.registry.sortedBy { it.menuSlot }.withIndex()) {
            val x = (i % 7) + 1
            val y = (i / 7) + 1
            inventory[x, y] = building.getItem(playerData)
        }
        inventory[4, 3] = clearItem
        event.player.openInventory(inventory)
        inventory.addInventoryCondition { player, slot, clickType, res ->
            res.isCancel = true
            var close = false
            if (res.clickedItem == clearItem) {
                close = true
                setItemSlot(player)
            } else if (res.clickedItem.hasTag(Tag.String("identifier"))) {
                val identifier = res.clickedItem.getTag(Tag.String("identifier"))
                val ref = Building.getBuildingByIdentifier(identifier)!!.playerRef.get(playerData.buildings)
                ref.select(player)
                close = true
            }
            if (close) {
                player.closeInventory()
            } else {
                inventory[slot] = res.clickedItem
            }
        }

        return true
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[4] = getItem(player.uuid, gameInstance)
    }

    fun updatePlayerItem(player: Player) {
        val data = player.data ?: return
        val identifier = player.inventory[4].getTag(Tag.String("identifier"))
        val building = Building.getBuildingByIdentifier(identifier)
        val ref = building?.playerRef?.get(data.buildings) ?: return
        ref.select(player)
    }
}