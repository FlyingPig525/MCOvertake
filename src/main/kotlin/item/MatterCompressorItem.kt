package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterCompressionPlant
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object MatterCompressorItem : Actionable {
    init {
        Actionable.registry += this
    }

    override val identifier: String = "mechanical:generator"
    override val itemMaterial: Material = MatterCompressionPlant.getItem(1, 1).material()

    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()] ?: return ERROR_ITEM
        return MatterCompressionPlant.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event, instance)
            return true
        }
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = players[event.player.uuid.toString()] ?: return true
        if (!checkBlockAvailable(playerData, target)) return true
        if (MatterCompressionPlant.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter - playerData.matterCompressorCost < 0) return true
        playerData.organicMatter -= playerData.matterCompressorCost
        playerData.matterCompressors.place(target, instance)
        playerData.matterCompressors.select(event.player, playerData.extractorCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()]!!
        data.matterCompressors.select(player, data.extractorCost)
    }
}