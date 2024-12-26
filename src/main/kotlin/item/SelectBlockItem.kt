package io.github.flyingpig525.item

import com.sun.jdi.InvalidTypeException
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.data.player.PlayerData.Companion.toBlockList
import io.github.flyingpig525.data.block.*
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
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*
import kotlin.enums.EnumEntries


object SelectBlockItem : Actionable {

    init {
        Actionable.registry += this
        Actionable.persistentRegistry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "item:select_block"
    override val itemMaterial: Material = Material.STRUCTURE_VOID


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<green>$COLONY_SYMBOL <bold>Select Block</bold> $COLONY_SYMBOL".asMini()
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Select Block")

        inventory[3, 1] = NATURAL_CATEGORY
        inventory[5, 1] = UNDERGROUND_CATEGORY
        inventory[4, 3] = NETHER_CATEGORY

        event.player.openInventory(inventory)

        val inventoryEventNode = EventNode.type("select-category-inv${event.player.uuid.mostSignificantBits}", EventFilter.INVENTORY, {_, inv -> inventory == inv}).listen<InventoryClickEvent> { e ->
            if (e.clickedItem.material() == Material.AIR) return@listen
            val gameInstance = instances.fromInstance(e.instance) ?: return@listen
            e.player.inventory.cursorItem = ItemStack.AIR
            when(e.clickedItem) {
                NATURAL_CATEGORY -> openCategory(NaturalCategory.entries, e, gameInstance)
                UNDERGROUND_CATEGORY -> openCategory(UndergroundCategory.entries, e, gameInstance)
                NETHER_CATEGORY -> openCategory(NetherCategory.entries, e, gameInstance)
                else -> {}
            }
            GlobalEventHandler.removeChildren("select-category-inv${event.player.uuid.mostSignificantBits}")
        }

        GlobalEventHandler.addChild(inventoryEventNode)

        return true
    }

    private fun openCategory(entries: EnumEntries<*>, e: InventoryClickEvent, instance: GameInstance) {
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

        val inventoryEventNode = EventNode.type(
            "select-block-inv${e.player.uuid.mostSignificantBits}",
            EventFilter.INVENTORY,
            {_, inv -> inventory == inv}
        )
            .listen<InventoryClickEvent> { e ->
            if (e.clickedItem.material() == Material.AIR || e.clickedItem.material().block() in instance.playerData.toBlockList()) return@listen
            if (instance.playerData[e.player.uuid.toString()] != null) {
                val data = instance.playerData[e.player.uuid.toString()]!!
                instance.clearBlock(data.block)
                e.player.hideBossBar(data.matterBossBar)
                e.player.hideBossBar(data.powerBossBar)
                e.player.hideBossBar(data.resourcesBossBar)
            }
            instance.playerData[e.player.uuid.toString()] =
                PlayerData(e.player.uuid.toString(), e.clickedItem.material().block()!!, e.player.username)
            e.player.closeInventory()
            for (i in 0..8) {
                e.player.inventory[i] = ItemStack.AIR
            }
            instance.playerData[e.player.uuid.toString()]!!.setupPlayer(e.player)
            GlobalEventHandler.removeChildren("select-block-inv${e.player.uuid.mostSignificantBits}")
        }

        GlobalEventHandler.addChild(inventoryEventNode)
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[8] = getItem(player.uuid, gameInstance)
    }

    fun setAllSlots(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        for (i in 0..8) {
            player.inventory[i] = getItem(player.uuid, gameInstance)
        }
    }
}