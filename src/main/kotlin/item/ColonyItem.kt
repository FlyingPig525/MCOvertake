package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.research.action.ActionData
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.tag.Tag
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

object ColonyItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }
    override val identifier: String = "block:colony"
    override val itemMaterial: Material = Material.CHEST


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val playerData = instance.playerData[uuid.toString()] ?: return ERROR_ITEM
        return item(itemMaterial) {
            itemName = "<green>$COLONY_SYMBOL<bold> Instantiate Colony</bold> <dark_gray>-<red> $POWER_SYMBOL ${playerData.colonyCost}".asMini()
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val gameInstance = instances.fromInstance(event.instance) ?: return true
        val data = gameInstance.playerData[event.player.uuid.toString()] ?: return true
        if (!data.colonyCooldown.isReady(Instant.now().toEpochMilli())) return true
        val actionData = ActionData.PlaceColony(data, event.instance, event.player).apply {
            cost = data.colonyCost
            cooldown = Cooldown(Duration.ofSeconds(15))
        }.also { data.research.onPlaceColony(it) }
        if (data.power - actionData.cost < 0) return true
        val target = event.player.getTrueTarget(20)?.playerPosition ?: return true
        if (event.instance.getBlock(target) == Block.GRASS_BLOCK) {
            claimWithParticle(event.player, target, Block.GRASS_BLOCK, data.block, gameInstance.instance)
            data.blocks++
            data.power -= actionData.cost
            data.colonyCooldown = actionData.cooldown
            event.player.sendPacket(
                SetCooldownPacket(
                    itemMaterial.cooldownIdentifier,
                    data.colonyCooldown.ticks
                )
            )
            data.updateBossBars()
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid, instances.fromInstance(player.instance) ?: return)
    }
}