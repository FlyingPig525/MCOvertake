package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.data.block.Head.Companion.withHead
import io.github.flyingpig525.instances
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.asPos
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*

object TeleportBackItem : Actionable {
    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "underground:back"
    override val itemMaterial: Material = Material.PLAYER_HEAD

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack =
        item(Material.PLAYER_HEAD) {
            itemName = "<white><bold>Go Back".asMini()
            setTag(Tag.String("identifier"), identifier)
        }.withHead("6ccbf9883dd359fdf2385c90a459d737765382ec4117b04895ac4dc4b60fc")

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val data = event.player.data ?: return true
        event.player.teleport(data.lastTeleporterPos.last().asPos())
        data.lastTeleporterPos.remove(data.lastTeleporterPos.last())
        if (data.lastTeleporterPos.isEmpty()) {
            event.player.inventory[2] = ItemStack.AIR
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[2] = getItem(player.uuid, gameInstance)
    }
}