package io.github.flyingpig525.item

import io.github.flyingpig525.*
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
import net.minestom.server.tag.Tag
import java.util.*

object UpgradeWallItem : Actionable {

    init {
        Actionable.registry += this
    }

    override val identifier: String = "building:upgrade_wall"


    override fun getItem(uuid: UUID): ItemStack {
        val target = instance.getPlayerByUuid(uuid)!!.getTrueTarget(20) ?: return ERROR_ITEM
        val upgradeCost = getWallUpgradeCost(instance.getBlock(target).defaultState()) ?: return ERROR_ITEM
        return item(Material.IRON_AXE) {
            itemName = "<gold>$WALL_SYMBOL <bold>Upgrade Wall</bold><dark_grey> - <green>$MATTER_SYMBOL $upgradeCost".asMini()
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()]!!
        val target = event.player.getTrueTarget(20)!!
        val block = instance.getBlock(target).defaultState()
        val level = block.wallLevel!!
        val cost = getWallUpgradeCost(block)!!
        if (data.organicMatter < cost) return true
        data.organicMatter -= cost
        instance.setBlock(target, nextWall(level))
        updateWall(target)
        repeatAdjacent(target, ::updateWall)
        return true
    }

    fun updateWall(point: Point) {
        val block = instance.getBlock(point)
        val level = block.wallLevel ?: return
        when(level) {
            1 -> { WallItem.updateIronBar(point) }

            in WOODEN_FENCE_RANGE -> {
                updateWallDirections(point, WOODEN_FENCE_RANGE)
            }

            in BRICK_FENCE_RANGE -> {
                updateWallDirections(point, BRICK_FENCE_RANGE)
            }

            in WALL_RANGE -> {
                updateWallDirections(point, WALL_RANGE)
            }

            in GLASS_PANE_RANGE -> {
                updateWallDirections(point, GLASS_PANE_RANGE)
            }
        }
    }

    fun updateWallDirections(point: Point, range: IntRange, block: Block = instance.getBlock(point).defaultState()) {
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
        player.inventory.set(0, getItem(player.uuid))
    }
}