package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterContainer
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag
import java.util.*

object MatterContainerItem : Actionable {

    init {
        Actionable.registry += this
    }
    override val identifier: String = "matter:container"

    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()]!!
        return MatterContainer.getItem(data).withTag(Tag.String("identifier"), identifier)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event, instance)
            return true
        }
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = players[event.player.uuid.toString()]!!
        if (!checkBlockAvailable(playerData, target)) return true
        if (MatterContainer.getResourceUse(playerData.matterContainers.count + 1) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter - playerData.containerCost < 0) return true
        playerData.organicMatter -= playerData.containerCost
        playerData.matterContainers.place(target, instance)
        playerData.matterContainers.select(event.player, playerData.containerCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()]!!
        data.matterContainers.select(player, data.containerCost)
    }
}