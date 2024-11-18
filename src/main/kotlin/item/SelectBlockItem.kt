package io.github.flyingpig525.item

import com.sun.jdi.InvalidTypeException
import io.github.flyingpig525.COLONY_SYMBOL
import io.github.flyingpig525.clearBlock
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.data.block.*
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
import kotlin.enums.EnumEntries


object SelectBlockItem : Actionable {

    init {
        Actionable.registry += this
        Actionable.persistentRegistry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.STRUCTURE_VOID) {
            itemName = "<green>$COLONY_SYMBOL <bold>Select Block</bold> $COLONY_SYMBOL".asMini()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Select Block")

        inventory[3, 1] = NATURAL_CATEGORY
        inventory[5, 1] = UNDERGROUND_CATEGORY
        inventory[4, 3] = NETHER_CATEGORY

        event.player.openInventory(inventory)

        val inventoryEventNode = EventNode.type("select-category-inv", EventFilter.INVENTORY, {_, inv -> inventory == inv}).listen<InventoryClickEvent> { e ->
            if (e.clickedItem.material() == Material.AIR) return@listen
            e.player.inventory.cursorItem = ItemStack.AIR
            when(e.clickedItem) {
                NATURAL_CATEGORY -> openCategory(NaturalCategory.entries, e)
                UNDERGROUND_CATEGORY -> openCategory(UndergroundCategory.entries, e)
                NETHER_CATEGORY -> openCategory(NetherCategory.entries, e)
                else -> {}
            }
//            players[e.player.uuid.toString()] = PlayerData(e.player.uuid.toString(), e.clickedItem.material().block()!!)
//            e.player.closeInventory()
//            for (i in 0..8) {
//                e.player.inventory[i] = ItemStack.AIR
//            }
//            players[e.player.uuid.toString()]!!.updateBossBars()
//            SelectBuildingItem.setItemSlot(e.player)
//            setItemSlot(e.player)
        }

        GlobalEventHandler.addChild(inventoryEventNode)

        return true
    }

    private fun openCategory(entries: EnumEntries<*>, e: InventoryClickEvent) {
        val inventory = Inventory(InventoryType.CHEST_6_ROW, "Select Block")


        for ((i, block) in entries.withIndex()) {
            if (block is CategoryBlock) {
                val item = item(block.material) {
                    itemName = "<gray>- <gold><bold>${block.name} <reset><gray>-".asMini()
                    lore {
                        +"<gray>-| <green><bold>Click to Select".asMini()
                    }
                }
                inventory[i % 9, i / 9] = item
            } else throw InvalidTypeException("block is not type CategoryBlock")
        }
        e.player.openInventory(inventory)

        val inventoryEventNode = EventNode.type("select-block-inv", EventFilter.INVENTORY, {_, inv -> inventory == inv}).listen<InventoryClickEvent> { e ->
            if (e.clickedItem.material() == Material.AIR) return@listen
            if (players[e.player.uuid.toString()] != null) {
                val data = players[e.player.uuid.toString()]!!
                clearBlock(data.block)
                e.player.hideBossBar(data.matterBossBar)
                e.player.hideBossBar(data.powerBossBar)
                e.player.hideBossBar(data.resourcesBossBar)
            }
            players[e.player.uuid.toString()] = PlayerData(e.player.uuid.toString(), e.clickedItem.material().block()!!)
            e.player.closeInventory()
            for (i in 0..8) {
                e.player.inventory[i] = ItemStack.AIR
            }
            players[e.player.uuid.toString()]!!.setupPlayer(e.player)
        }

        GlobalEventHandler.addChild(inventoryEventNode)
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