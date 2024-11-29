package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.data.PlayerData
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.NumberBinaryTag
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.NBTComponentBuilder
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
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
import net.minestom.server.tag.Tag
import java.util.*

object ClaimWaterItem : Actionable {
    init {
        Actionable.registry += this
    }

    override val identifier: String = "block:claim_water"

    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.WOODEN_AXE) {
            itemName = "<gold>$WALL_SYMBOL <bold>Build Raft</bold><dark_grey> - <red>$POWER_SYMBOL 500".asMini()
        }.withTag(Tag.String("identifier"), identifier)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        // Ensure can claim
        val data = players[event.player.uuid.toString()] ?: return true
        val target = event.player.getTrueTarget(20) ?: return true
        // Claim logic
        instance.setBlock(target.withY(38.0), data.block)
        spawnPlayerRaft(data.block, target.withY(40.0))
        instance.setBlock(target.withY(40.0), Block.LILY_PAD)
        data.blocks++
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