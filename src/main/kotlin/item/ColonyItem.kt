package io.github.flyingpig525.item

import io.github.flyingpig525.COLONY_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.players
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.tag.Tag
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.UUID

object ColonyItem : Actionable {

    init {
        Actionable.registry += this
    }
    override val identifier: String = "block:colony"


    override fun getItem(uuid: UUID): ItemStack {
        val playerData = players[uuid.toString()]!!
        return item(Material.CHEST) {
            itemName = "<green>$COLONY_SYMBOL<bold> Instantiate Colony</bold> <dark_gray>-<red> $POWER_SYMBOL ${playerData.colonyCost}".asMini()
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()]!!
        if (data.power - data.colonyCost < 0) return true
        if (!data.colonyCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        if (instance.getBlock(target) == Block.GRASS_BLOCK) {
            claimWithParticle(event.player, target, Block.GRASS_BLOCK, data.block)
            data.blocks++
            data.power -= data.colonyCost
            data.colonyCooldown = Cooldown(Duration.ofSeconds(15))
            event.player.sendPacket(
                SetCooldownPacket(
                    getItem(event.player.uuid).material().id(),
                    (data.colonyCooldown.duration.toMillis() / 50).toInt()
                )
            )
            data.updateBossBars()
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}