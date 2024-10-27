package io.github.flyingpig525.item

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.WALL_SYMBOL
import io.github.flyingpig525.building.TrainingCamp
import io.github.flyingpig525.instance
import io.github.flyingpig525.players
import io.github.flyingpig525.wall.getWallUpgradeCost
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object UpgradeWallItem : Actionable {

    init {
        Actionable.registry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()]!!
        val target = instance.getPlayerByUuid(uuid)!!.getTargetBlockPosition(20) ?: throw IllegalStateException("no player target???")
        val upgradeCost = getWallUpgradeCost(instance.getBlock(target).defaultState()) ?: return item(Material.BARRIER) {
            itemName = "<red><bold>ERROR".asMini()
        }
        return item(Material.IRON_AXE) {
            itemName = "<gold>$WALL_SYMBOL <bold>Upgrade Wall</bold><dark_grey> - <green>$MATTER_SYMBOL $upgradeCost".asMini()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
//        val target = event.player.getTargetBlockPosition(20) ?: return true
//        val playerData = players[event.player.uuid.toString()]!!
//        if (instance.getBlock(target.sub(0.0, 1.0, 0.0)) != playerData.block) return true
//
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}