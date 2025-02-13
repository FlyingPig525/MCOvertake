package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.config
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.config.BlockConfig
import io.github.flyingpig525.data.player.config.ConfigElement
import io.github.flyingpig525.data.player.config.PlayerConfig
import io.github.flyingpig525.ksp.Item
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
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*

@Item(persistent = true)
object PlayerConfigItem : Actionable {

    override val identifier: String = "player:config"
    override val itemMaterial: Material = Material.FLOWER_BANNER_PATTERN

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "Player Config".asMini()
            lore {
                +"<dark_gray>Contains configurable settings to tweak".asMini().noItalic()
                +"<dark_gray>the MCOvertake experience.".asMini().noItalic()
            }
            setTag(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Player Config")
        val black = item(Material.BLACK_STAINED_GLASS_PANE) { itemName = "".asMini() }
        for (i in 0..(8 + 9 * 4)) {
            inventory[i] = black
        }

        if (event.player.config == null) {
            event.instance.gameInstance!!.playerConfigs[event.player.uuid.toString()] = PlayerConfig()
        }
        val config = event.player.config!!

        var i = 0
        for (el in config.map()) {

            val value = el.value.get(config)
            if (value !is ConfigElement) continue
            inventory[i*2] = value.iconWithValue.withTag(Tag.String("name"), el.key)
            i++
        }
        if (event.instance.gameInstance!!.uuidParents[event.player.uuid.toString()] == event.player.uuid.toString()) {
            inventory[4, 4] = item(Material.WHITE_STAINED_GLASS_PANE) {
                itemName = "Block Config".asMini()
                setTag(Tag.Boolean("block_config"), true)
            }
        }

        inventory.addInventoryCondition { player, slot, type, res ->
            res.isCancel = true
            if (res.clickedItem.hasTag(Tag.Boolean("block_config"))) {
                player.closeInventory()
                blockConfig(event)
                return@addInventoryCondition
            }
            val name = res.clickedItem.getTag(Tag.String("name")) ?: return@addInventoryCondition
            val ref = config.map()[name]?.get(config) as ConfigElement? ?: return@addInventoryCondition
            val fn = PlayerConfig.onChangeFunctions[name]
            if (fn != null) {
                fn(player, ref)
            } else {
                ref.value = !ref.value
            }
            player.openInventory!![slot] = ref.iconWithValue.withTag(Tag.String("name"), name)
        }
        event.player.openInventory(inventory)
        return true
    }

    fun blockConfig(event: PlayerUseItemEvent) {
        val data = event.player.data ?: return

        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Block Config")
        val black = item(Material.BLACK_STAINED_GLASS_PANE) { itemName = "".asMini() }
        for (i in 0..(8 + 9 * 4)) {
            inventory[i] = black
        }

        var i = 0
        for (el in data.blockConfig.map()) {
            val value = el.value.get(data.blockConfig)
            if (value !is ConfigElement) continue
            inventory[i*2] = value.iconWithValue.withTag(Tag.String("name"), el.key)
            i++
        }

        inventory[4, 4] = item(Material.WHITE_STAINED_GLASS_PANE) {
            itemName = "Player Config".asMini()
            setTag(Tag.Boolean("player_config"), true)
        }

        inventory.addInventoryCondition { player, slot, type, res ->
            res.isCancel = true
            if (res.clickedItem.hasTag(Tag.Boolean("player_config"))) {
                player.closeInventory()
                onInteract(event)
                return@addInventoryCondition
            }
            val name = res.clickedItem.getTag(Tag.String("name")) ?: return@addInventoryCondition
            val ref = data.blockConfig.map()[name]?.get(data.blockConfig) as ConfigElement? ?: return@addInventoryCondition
            val fn = BlockConfig.onChangeFunctions[name]
            if (fn != null) {
                fn(player, ref)
            } else {
                ref.value = !ref.value
            }
            player.openInventory!![slot] = ref.iconWithValue.withTag(Tag.String("name"), name)
        }
        event.player.openInventory(inventory)
    }

    override fun setItemSlot(player: Player) {
        player.inventory[8] = getItem(player.uuid, player.gameInstance!!)
    }
}