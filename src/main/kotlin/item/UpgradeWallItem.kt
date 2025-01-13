package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.wall.*
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
import java.time.Instant
import java.util.*

object UpgradeWallItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "building:upgrade_wall"
    override val itemMaterial: Material = Material.IRON_AXE


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val target = instance.instance.getPlayerByUuid(uuid)!!.getTrueTarget(20) ?: return ERROR_ITEM
        val block = instance.instance.getBlock(target.buildingPosition)
        val upgradeCost = getWallUpgradeCost(block) ?: return ERROR_ITEM
        return item(itemMaterial) {
            itemName = "<gold>$WALL_SYMBOL <bold>Upgrade Wall</bold><dark_grey> - <green>$MATTER_SYMBOL $upgradeCost".asMini()
            if (!block.canUpgradeWall) {
                itemName = "<gold>$WALL_SYMBOL <bold>Max Level".asMini()
            }
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val gameInstance = instance.gameInstance ?: return true
        val data = gameInstance.playerData[event.player.uuid.toString()] ?: return true
        if (!data.wallUpgradeCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20)?.buildingPosition ?: return true
        val block = instance.getBlock(target).defaultState()
        if (!block.canUpgradeWall) return true
        val level = block.wallLevel
        val cost = getWallUpgradeCost(block) ?: return true
        val actionData = ActionData.UpgradeWall(data, instance, event.player).apply {
            this.cost = cost
        }.also { data.research.onUpgradeWall(it) }
        if (data.organicMatter < actionData.cost) return true
        data.organicMatter -= actionData.cost
        data.wallUpgradeCooldown = actionData.cooldown
        event.player.sendPacket(
            SetCooldownPacket(
                itemMaterial.cooldownIdentifier,
                data.wallUpgradeCooldown.ticks
            )
        )
        instance.setBlock(target, nextWall(level))
        updateWall(target, instance)
        target.repeatAdjacent { updateWall(it, gameInstance.instance) }
        return true
    }

    fun updateWall(point: Point, instance: Instance) {
        val block = instance.getBlock(point)
        val level = block.wallLevel
        when(level) {
            1 -> { WallItem.updateIronBar(point, instance) }

            in WOODEN_FENCE_RANGE -> {
                updateWallDirections(instance, point, WOODEN_FENCE_RANGE)
            }

            in BRICK_FENCE_RANGE -> {
                updateWallDirections(instance, point, BRICK_FENCE_RANGE)
            }

            in WALL_RANGE -> {
                updateWallDirections(instance, point, WALL_RANGE)
            }

            in GLASS_PANE_RANGE -> {
                updateWallDirections(instance, point, GLASS_PANE_RANGE)
            }
        }
    }

    private fun updateWallDirections(instance: Instance, point: Point, range: IntRange, block: Block = instance.getBlock(point).defaultState()) {
        val north = instance.getBlock(point.add(0.0, 0.0, -1.0)).defaultState().wallLevel in range
        val south = instance.getBlock(point.add(0.0, 0.0, 1.0)).defaultState().wallLevel in range
        val east = instance.getBlock(point.add(1.0, 0.0, 0.0)).defaultState().wallLevel in range
        val west = instance.getBlock(point.add(-1.0, 0.0, 0.0)).defaultState().wallLevel in range

        val block = block.withProperties(mapOf(
            "north" to if (range == WALL_RANGE) if (north) "low" else "none" else "$north",
            "south" to if (range == WALL_RANGE) if (south) "low" else "none" else "$south",
            "east" to if (range == WALL_RANGE) if (east) "low" else "none" else "$east",
            "west" to if (range == WALL_RANGE) if (west) "low" else "none" else "$west"
        ))
        instance.setBlock(point, block)
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[0] = getItem(player.uuid, gameInstance)
    }
}