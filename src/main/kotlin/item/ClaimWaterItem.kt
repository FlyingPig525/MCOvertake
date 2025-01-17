package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.research.action.ActionData
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
import net.minestom.server.instance.Instance
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

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return item(itemMaterial) {
            itemName = "<gold>$WALL_SYMBOL <bold>Build Raft</bold><dark_grey> - <green>$MATTER_SYMBOL ${data.raftCost}".asMini()
        }.withTag(Tag.String("identifier"), identifier)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val data = event.player.data ?: return true
        if (!data.raftCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        if (target.isUnderground) return true
        val actionData = ActionData.PlaceRaft(data, event.instance, event.player).apply {
            cooldown = Cooldown(Duration.ofSeconds(20))
            cost = data.raftCost
        }.also { data.research.onPlaceRaft(it) }
        if (data.organicMatter < actionData.cost) return true
        // Claim logic
        event.instance.setBlock(target.playerPosition, data.block)
        event.instance.setBlock(target.withY(40.0), Block.LILY_PAD)
        spawnPlayerRaft(data.block, target.withY(40.0), event.instance)
        data.blocks++
        data.raftCooldown = actionData.cooldown
        data.sendPacket(
            SetCooldownPacket(
                itemMaterial.cooldownIdentifier,
                actionData.cooldown.ticks
            )
        )
        return true
    }

    fun spawnPlayerRaft(playerBlock: Block, point: Point, instance: Instance) {
        Entity(EntityType.BLOCK_DISPLAY).also {
            it.hasGravity = false
            with((it.entityMeta as BlockDisplayMeta)) {
                setBlockState(Block.SPRUCE_TRAPDOOR)
                scale = Vec(0.9, 0.866, 0.9)
                translation = Vec(0.05, -0.15125, 0.05)
            }
            it.setTag(Tag.Boolean("player_raft"), true)
            it.setInstance(instance, point)
        }
        Entity(EntityType.BLOCK_DISPLAY).also {
            it.hasGravity = false
            with((it.entityMeta as BlockDisplayMeta)) {
                setBlockState(playerBlock)
                scale = Vec(0.875, 0.1625, 0.875)
                translation = Vec(-0.4375+0.5, -0.145, -0.4375+0.5)
            }
            it.setTag(Tag.Boolean("player_raft"), true)
            it.setInstance(instance, point)
        }
    }

    fun destroyPlayerRaft(point: Point, instance: Instance) =
        instance.getNearbyEntities(point, 0.2).forEach {
            if (it.hasTag(Tag.String("player_raft"))) it.remove()
        }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid, instances.fromInstance(player.instance) ?: return)
    }
}