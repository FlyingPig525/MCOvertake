package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.ksp.Item
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
import net.minestom.server.tag.Tag
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

@Item(persistent = true)
object WallItem : Actionable {
    override val identifier: String = "building:wall"
    override val itemMaterial: Material = Material.IRON_BARS

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<white>$WALL_SYMBOL <bold>Build Wall</bold> <dark_gray>-<green> $MATTER_SYMBOL 15".asMini()
            amount = 1
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data = event.player.data ?: return true
        if (data.organicMatter < 15) {
            event.player.sendMessage("<red><bold>Not enough Organic Matter</bold> (${data.organicMatter}/15)".asMini())
            return true
        }
        if (!data.wallCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        if (!checkBlockAvailable(data, target, instance)) return true
        val actionData = ActionData.BuildWall(data, instance, event.player).apply {
            cost = 15
            cooldown = Cooldown(Duration.ofMillis(500))
        }.also { data.research.onBuildWall(it) }
        data.organicMatter -= actionData.cost
        data.wallCooldown = actionData.cooldown

        updateIronBar(target.buildingPosition, instance)
        target.buildingPosition.repeatAdjacent {
            if (instance.getBlock(it).defaultState() == Block.IRON_BARS) {
                updateIronBar(it, instance)
            }
        }

        data.sendPacket(
            SetCooldownPacket(
                itemMaterial.cooldownIdentifier,
                data.wallCooldown.ticks
            )
        )
        return true
    }

    fun updateIronBar(point: Point, instance: Instance) {
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
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[3] = getItem(player.uuid, gameInstance)
    }
}