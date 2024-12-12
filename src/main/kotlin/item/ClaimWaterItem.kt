package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.BlockDisplayMeta
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

object ClaimWaterItem : Actionable {
    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "block:claim_water"
    override val itemMaterial: Material = Material.WOODEN_AXE

    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()] ?: return ERROR_ITEM
        return item(itemMaterial) {
            itemName = "<gold>$WALL_SYMBOL <bold>Build Raft</bold><dark_grey> - <green>$MATTER_SYMBOL ${data.raftCost}".asMini()
        }.withTag(Tag.String("identifier"), identifier)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        // TODO: ADD COST AND STUFF
        // Ensure can claim
        val data = players[event.player.uuid.toString()] ?: return true
        if (!data.raftCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        if (target.isUnderground) return true
        if (data.organicMatter < data.raftCost) return true
        // Claim logic
        instance.setBlock(target.playerPosition, data.block)
        instance.setBlock(target.withY(40.0), Block.LILY_PAD)
        spawnPlayerRaft(data.block, target.withY(40.0))
        data.blocks++
        data.raftCooldown = Cooldown(Duration.ofSeconds(20))
        event.player.sendPacket(
            SetCooldownPacket(
                itemMaterial.cooldownIdentifier,
                data.raftCooldown.ticks
            )
        )
        return true
    }

    fun spawnPlayerRaft(playerBlock: Block, point: Point) {
        Entity(EntityType.BLOCK_DISPLAY).also {
            it.hasGravity = false
            with((it.entityMeta as BlockDisplayMeta)) {
                setBlockState(Block.SPRUCE_TRAPDOOR)
                scale = Vec(0.9, 0.866, 0.9)
                translation = Vec(0.05, -0.15125, 0.05)
            }
            it.setInstance(instance, point)
        }
        Entity(EntityType.BLOCK_DISPLAY).also {
            it.hasGravity = false
            with((it.entityMeta as BlockDisplayMeta)) {
                setBlockState(playerBlock)
                scale = Vec(0.875, 0.1625, 0.875)
                translation = Vec(-0.4375+0.5, -0.15125, -0.4375+0.5)
            }
            it.setInstance(instance, point)
        }
    }

    fun destroyPlayerRaft(point: Point) =
        instance.getNearbyEntities(point, 0.2).forEach { it.remove() }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}