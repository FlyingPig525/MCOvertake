package io.github.flyingpig525.item

import io.github.flyingpig525.building.Barrack
import io.github.flyingpig525.building.MatterContainer
import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import java.util.*

object BarracksItem : Actionable {

    init {
        Actionable.registry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()]!!
        return Barrack.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event, instance)
            return true
        }
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val playerData = players[event.player.uuid.toString()]!!
        if (Barrack.getResourceUse(playerData.barracks.count + 1) > playerData.maxDisposableResources * 1.5) return true
        if (instance.getBlock(target) != playerData.block) return true
        if (playerData.organicMatter - playerData.barracksCost < 0) return true
        playerData.organicMatter -= playerData.barracksCost
        playerData.barracks.place(target, instance)
        playerData.barracks.select(event.player, playerData.barracksCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()]!!
        data.barracks.select(player, data.barracksCost)
    }
}