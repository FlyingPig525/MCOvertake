package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterExtractor
import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import java.util.*

object MatterExtractorItem : Actionable {
    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()]!!
        return MatterExtractor.getItem(data.extractorCost)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event, instance)
            return true
        }
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val playerData = players[event.player.uuid.toString()]!!
        if (instance.getBlock(target) != playerData.block) return true
        if (playerData.organicMatter - playerData.extractorCost < 0) return true
        playerData.organicMatter -= playerData.extractorCost
        playerData.matterExtractors.place(target, instance)
        playerData.matterExtractors.select(event.player, playerData.extractorCost)
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()]!!
        data.matterExtractors.select(player, data.extractorCost)
    }
}