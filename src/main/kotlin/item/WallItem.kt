package io.github.flyingpig525.item

import io.github.flyingpig525.*
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

object WallItem : Actionable {

    init {
        Actionable.registry += this
        Actionable.persistentRegistry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.IRON_BARS) {
            itemName = "<white>$WALL_SYMBOL <bold>Build Wall</bold> <dark_gray>-<green> $MATTER_SYMBOL 15".asMini()
            amount = 1
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()]!!
        if (data.organicMatter - 15 < 0) return true
        if (!data.wallCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        if (instance.getBlock(target) == data.block && instance.getBlock(target.add(0.0, 1.0, 0.0)) == Block.AIR) {
            data.organicMatter -= 15
            data.wallCooldown = Cooldown(Duration.ofMillis(500))

            updateIronBar(target.add(0.0, 1.0, 0.0))
            repeatAdjacent(target.add(0.0, 1.0, 0.0)) {
                if (instance.getBlock(it).defaultState() == Block.IRON_BARS) {
                    updateIronBar(it)
                }
            }

            event.player.sendPacket(
                SetCooldownPacket(
                    getItem(event.player.uuid).material().id(),
                    (data.wallCooldown.duration.toMillis() / 50).toInt()
                )
            )
        }
        return true
    }

    fun updateIronBar(point: Point) {
        val north = instance.getBlock(point.add(0.0, 0.0, -1.0)).defaultState() == Block.IRON_BARS
        val south = instance.getBlock(point.add(0.0, 0.0, 1.0)).defaultState() == Block.IRON_BARS
        val east = instance.getBlock(point.add(1.0, 0.0, 0.0)).defaultState() == Block.IRON_BARS
        val west = instance.getBlock(point.add(-1.0, 0.0, 0.0)).defaultState() == Block.IRON_BARS

        val block = Block.IRON_BARS.withProperties(mapOf(
            "north" to "$north",
            "south" to "$south",
            "east" to "$east",
            "west" to "$west"
        ))
        instance.setBlock(point, block)
    }

    override fun setItemSlot(player: Player) {
        player.inventory[3] = getItem(player.uuid)
    }
}