package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.data.inventory.InventoryConditionArguments.Companion.addInventoryCondition
import io.github.flyingpig525.instances
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

object PlayerConfigItem : Actionable {
    init {
        Actionable.registry += this
        Actionable.persistentRegistry += this
    }

    override val identifier: String = "player:config"
    override val itemMaterial: Material = Material.FLOWER_BANNER_PATTERN

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "Player Settings".asMini()
            lore {
                +"<dark_gray>Contains configurable settings to tweak".asMini().noItalic()
                +"<dark_gray>the MCOvertake experience.".asMini().noItalic()
            }
            setTag(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val data = event.player.data ?: return true

        val inventory = Inventory(InventoryType.CHEST_5_ROW, "Player Settings")
        val black = item(Material.BLACK_STAINED_GLASS_PANE) { itemName = "".asMini() }
        for (i in 0..(8 + 9 * 4)) {
            inventory[i] = black
        }
        for ((i, el) in data.playerConfig.map().entries.withIndex()) {
            inventory[i*2] = el.value.get().iconWithValue.withTag(Tag.String("name"), el.key)
        }
        inventory.addInventoryCondition { player, slot, type, res ->
            res.isCancel = true
            val name = res.clickedItem.getTag(Tag.String("name")) ?: return@addInventoryCondition
            val ref = data.playerConfig.map()[name]?.get() ?: return@addInventoryCondition
            ref.value = !ref.value
            player.openInventory!![slot] = ref.iconWithValue.withTag(Tag.String("name"), name)
        }
        event.player.openInventory(inventory)

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[8] = getItem(player.uuid, player.gameInstance!!)
    }
}