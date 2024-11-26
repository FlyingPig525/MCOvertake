package io.github.flyingpig525.item

import io.github.flyingpig525.CLAIM_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.players
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.ParticleBuilder
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.particle
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.instance.block.BlockHandler.Destroy
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.particle.Particle
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID
import net.minestom.server.utils.PacketUtils
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

object ClaimItem : Actionable {

    init {
        Actionable.registry += this
    }

    override val identifier: String = "block:claim"


    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()] ?: return ERROR_ITEM
        val item = listOf(Material.WOODEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE)[data.claimLevel]
        return item(item) {
            itemName = "<gold>$CLAIM_SYMBOL <bold>Expand</bold> <dark_gray>-<red> $POWER_SYMBOL ${data.claimCost}".asMini()
            amount = 1
            set(Tag.String("identifier"), identifier)
        }

    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()] ?: return true
        if (data.power - data.claimCost < 0) return true
        if (!data.claimCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        if (instance.getBlock(target) == Block.GRASS_BLOCK) {
            claimWithParticle(event.player, target, Block.GRASS_BLOCK, data.block)
            data.blocks++
            data.power -= data.claimCost
            data.claimCooldown = Cooldown(Duration.ofMillis(data.maxClaimCooldown))
            event.player.sendPacket(
                SetCooldownPacket(
                    getItem(event.player.uuid).material().id(),
                    (data.claimCooldown.duration.toMillis() / 50).toInt()
                ))
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}