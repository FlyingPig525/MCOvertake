package io.github.flyingpig525.item

import io.github.flyingpig525.GLOBAL_RESEARCH_SYMBOL
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import io.github.flyingpig525.instances
import io.github.flyingpig525.ksp.Item
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.PotionContents
import net.minestom.server.potion.PotionType
import net.minestom.server.sound.SoundEvent
import net.minestom.server.tag.Tag
import java.util.*

@Item(persistent = true)
object ResearchUpgradeItem : Actionable {

    override val identifier: String = "research:upgrades"
    override val itemMaterial: Material = Material.LINGERING_POTION


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            set(Tag.String("identifier"), identifier)
        }.with(ItemComponent.POTION_CONTENTS, PotionContents(PotionType.AWKWARD, NamedTextColor.AQUA))
            // item name set outside of builder because setting potion contents sets the item name
            // that also makes it have to be custom name and not item name
            .with(ItemComponent.CUSTOM_NAME, "<aqua>$GLOBAL_RESEARCH_SYMBOL <bold>Research Upgrades".asMini().noItalic())
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val data = event.player.data ?: return true
        val inventory = Inventory(InventoryType.CHEST_1_ROW, "Research Type")

        for ((i, currency) in data.research.filter { it.currencyLevel != 0 }.withIndex()) {
            inventory[i] = item(currency.colorItem) {
                itemName = "<${currency.color}>${currency.symbol} <gray>-<${currency.color}> ${currency.count}".asMini()
                setTag(Tag.Integer("currencyId"), currency.currencyLevel)
            }
        }
        inventory.addInventoryCondition { player, i, clickType, res ->
            res.isCancel = true
            val researchId = res.clickedItem.getTag(Tag.Integer("currencyId")) ?: return@addInventoryCondition
            val currency = data.research.currencyById(researchId)!!
            currencyInventory(event, currency)
        }
        event.player.openInventory(inventory)

        return true
    }

    private fun currencyInventory(e: PlayerUseItemEvent, currency: ResearchCurrency) {
        val inventory = Inventory(
            InventoryType.CHEST_6_ROW,
            "<${currency.color}>${currency.symbol}</${currency.color}> - ${currency.count}".asMini()
        )

        for ((i, upgrade) in currency.upgrades.withIndex()) {
            val newItem = upgrade.item().withTag(Tag.String("name"), upgrade.name)
            val lore = newItem.get(ItemComponent.LORE)!!.map {
                if (it.toString().contains("Cost:") && upgrade.level == upgrade.maxLevel) {
                    return@map "<green><bold>Max Level".asMini().noItalic()
                }
                it
            }
            inventory[i * 2] = newItem.withLore(lore)
        }
        inventory.addInventoryCondition { player, slot, clickType, res ->
            res.isCancel = true
            val name = res.clickedItem.getTag(Tag.String("name")) ?: return@addInventoryCondition
            val upgrade = currency.upgradeByName(name) ?: return@addInventoryCondition
            val purchaseState = upgrade.onPurchase(res, currency, player)
            if (purchaseState !is ResearchUpgrade.PurchaseState.Success) {
                e.player.sendMessage(purchaseState.toString().asMini())
                e.player.playSound(
                    Sound.sound(
                        SoundEvent.ENTITY_VILLAGER_NO.key(),
                        Sound.Source.PLAYER,
                        1f,
                        1f
                    )
                )
                e.player.playSound(
                    Sound.sound(
                        SoundEvent.ITEM_SHIELD_BLOCK.key(),
                        Sound.Source.PLAYER,
                        1f,
                        1f
                    )
                )
                return@addInventoryCondition
            }
            (player.openInventory!! as Inventory).apply {
                val newItem = upgrade.item().withTag(Tag.String("name"), upgrade.name)
                val lore = newItem.get(ItemComponent.LORE)!!.map {
                    if (it.toString().contains("Cost:") && upgrade.level == upgrade.maxLevel) {
                        return@map "<green><bold>Max Level".asMini().noItalic()
                    }
                    it
                }
                set(slot, newItem.withLore(lore))
                title = "<${currency.color}>${currency.symbol}</${currency.color}> - ${currency.count}".asMini()
            }
            e.player.playSound(
                Sound.sound(
                    SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP.key(),
                    Sound.Source.PLAYER,
                    1f,
                    ((95..105).random() / 100f)
                )
            )
        }
        e.player.closeInventory()
        e.player.openInventory(inventory)
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[6] = getItem(player.uuid, gameInstance)
    }
}