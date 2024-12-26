package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.PotionContents
import net.minestom.server.potion.PotionType
import net.minestom.server.tag.Tag
import java.util.*

object ResearchUpgradeItem : Actionable {

    init {
        Actionable.registry += this
        Actionable.persistentRegistry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "research:upgrades"
    override val itemMaterial: Material = Material.POTION


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<aqua>$GLOBAL_RESEARCH_SYMBOL <bold>Research Upgrades</bold>".asMini()
            set(Tag.String("identifier"), identifier)
        }.with(ItemComponent.POTION_CONTENTS, PotionContents(PotionType.AWKWARD, NamedTextColor.AQUA))
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val gameInstance = instances.fromInstance(event.instance) ?: return true
        val data = gameInstance.playerData[event.player.uuid.toString()] ?: return true
        val inventory = Inventory(InventoryType.CHEST_1_ROW, "Research Type")

        for ((i, currency) in data.research.withIndex()) {
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
        e.instance.eventNode().removeChildren("purchase-upgrade-inv${e.player.uuid.mostSignificantBits}")
        val inventory = Inventory(
            InventoryType.CHEST_6_ROW,
            "<${currency.color}>${currency.symbol}</${currency.color}> - ${currency.count}".asMini()
        )

        for ((i, upgrade) in currency.upgrades.withIndex()) {
            if (upgrade.requiredInternalLevel > currency.currencyLevel) continue
            inventory[i * 2] = upgrade.item().withTag(Tag.String("name"), upgrade.name)
        }
        inventory.addInventoryCondition { player, i, clickType, res ->
            res.isCancel = true
            val name = res.clickedItem.getTag(Tag.String("name")) ?: return@addInventoryCondition
            val upgrade = currency.upgradeByName(name) ?: return@addInventoryCondition
            val purchaseState = upgrade.onPurchase(res, currency)
            if (purchaseState !is ResearchUpgrade.PurchaseState.Success) {
                e.player.sendMessage(purchaseState.toString().asMini())
                return@addInventoryCondition
            }
            e.instance.eventNode().removeChildren("purchase-upgrade-inv${e.player.uuid.mostSignificantBits}")
            currencyInventory(e, currency)
        }
        e.player.closeInventory()
        e.player.openInventory(inventory)
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[6] = getItem(player.uuid, gameInstance)
    }
}