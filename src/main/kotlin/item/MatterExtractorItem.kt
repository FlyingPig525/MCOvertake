package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterContainer
import io.github.flyingpig525.building.MatterExtractor
import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import java.util.*

object MatterExtractorItem : Actionable {

    init {
        Actionable.registry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()]!!
        return MatterExtractor.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event, instance)
            return true
        }
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val playerData = players[event.player.uuid.toString()]!!
        if (MatterExtractor.getResourceUse(playerData.matterExtractors.count + 1) > playerData.maxDisposableResources) return true

        if (instance.getBlock(target) != playerData.block) return true
        if (playerData.organicMatter - playerData.extractorCost < 0) return true
        playerData.organicMatter -= playerData.extractorCost
        playerData.matterExtractors.place(target, instance)
        playerData.matterExtractors.select(event.player, playerData.extractorCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()]!!
        data.matterExtractors.select(player, data.extractorCost)
    }
}