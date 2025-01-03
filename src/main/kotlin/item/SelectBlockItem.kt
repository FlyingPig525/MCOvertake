package io.github.flyingpig525.item

import com.sun.jdi.InvalidTypeException
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.data.player.PlayerData.Companion.toBlockList
import io.github.flyingpig525.data.block.*
import io.github.flyingpig525.data.inventory.InventoryConditionArguments
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.inventory.condition.InventoryConditionResult
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
        val gameInstance = instances.fromInstance(event.instance) ?: return true
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Select Block")

        inventory[3, 1] = NATURAL_CATEGORY
        inventory[5, 1] = UNDERGROUND_CATEGORY
        inventory[4, 3] = NETHER_CATEGORY

        inventory.addInventoryCondition { player: Player, slot: Int, clickType: ClickType, res: InventoryConditionResult ->
            res.isCancel = true
            if (res.clickedItem.material() == Material.AIR) return@addInventoryCondition
            player.inventory.cursorItem = ItemStack.AIR
            val arguments = InventoryConditionArguments(player, slot, clickType, res)
            when(res.clickedItem) {
                NATURAL_CATEGORY -> openCategory(NaturalCategory.entries, arguments, gameInstance)
                UNDERGROUND_CATEGORY -> openCategory(UndergroundCategory.entries, arguments, gameInstance)
                NETHER_CATEGORY -> openCategory(NetherCategory.entries, arguments, gameInstance)
                else -> {}
            }
        }

        event.player.openInventory(inventory)
        return true
    }

    private fun openCategory(entries: EnumEntries<*>, e: InventoryConditionArguments, instance: GameInstance) {
        val inventory = Inventory(InventoryType.CHEST_6_ROW, "Select Block")


        for ((i, cBlock) in entries.withIndex()) {
            if (cBlock is CategoryBlock) {
                val item = item(cBlock.material) {
                    itemName = "<gray>- <gold><bold>${cBlock.name.replace('_', ' ')} <reset><gray>-".asMini()
                    lore {
                        if (cBlock.block !in instance.playerData.toBlockList()) {
                            +"<gray>-| <green><bold><i>Click to Select<reset> <gray>|-".asMini().noItalic()
                        } else {
                            +"<gray>-| <red><bold><i>Block already in use<reset> <gray>|-".asMini().noItalic()
                        }
                    }
                }
                inventory[i % 9, i / 9] = item
            } else throw InvalidTypeException("block is not type CategoryBlock")
        }

        inventory.addInventoryCondition { player: Player, slot: Int, clickType: ClickType, res: InventoryConditionResult ->
            res.isCancel = true
            if (res.clickedItem.material() == Material.AIR
                || res.clickedItem.material().block() in instance.playerData.toBlockList()) return@addInventoryCondition
            if (instance.playerData[e.player.uuid.toString()] != null) {
                val data = instance.playerData[e.player.uuid.toString()]!!
                instance.clearBlock(data.block)
                e.player.hideBossBar(data.matterBossBar)
                e.player.hideBossBar(data.powerBossBar)
                e.player.hideBossBar(data.resourcesBossBar)
            }
            instance.playerData[e.player.uuid.toString()] =
                PlayerData(e.player.uuid.toString(), res.clickedItem.material().block()!!, e.player.username)
            e.player.closeInventory()
            for (i in 0..8) {
                e.player.inventory[i] = ItemStack.AIR
            }
            instance.playerData[e.player.uuid.toString()]!!.gameInstance = instance
            instance.playerData[e.player.uuid.toString()]!!.setupPlayer(e.player)
        }
        e.player.openInventory(inventory)
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