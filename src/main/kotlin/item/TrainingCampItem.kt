package io.github.flyingpig525.item

import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import java.util.*

object TrainingCampItem : Actionable {
    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid]!!
        return data.trainingCamps.item(data.trainingCampCost)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event, instance)
            return true
        }
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val playerData = players[event.player.uuid]!!
        if (instance.getBlock(target) != playerData.block) return true
        if (playerData.organicMatter - playerData.trainingCampCost < 0) return true
        playerData.organicMatter -= playerData.trainingCampCost
        playerData.trainingCamps.place(target, instance)
        playerData.trainingCamps.setBuildingItem(event.player.inventory, playerData.trainingCampCost)
        playerData.updateBossBars(event.player)
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid]!!
        data.trainingCamps.setBuildingItem(player.inventory, data.trainingCampCost)
    }
}