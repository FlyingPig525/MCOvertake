package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.MatterContainer
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.instances
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object MatterContainerItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }
    override val identifier: String = "matter:container"
    override val itemMaterial: Material = MatterContainer.getItem(1, 1).material()

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.playerData[uuid.toString()] ?: return ERROR_ITEM
        return MatterContainer.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event)
            return true
        }
        val target = event.player.getTrueTarget(20) ?: return true
        val gameInstance = instances.fromInstance(event.instance) ?: return true
        val playerData = gameInstance.playerData[event.player.uuid.toString()] ?: return true
        if (!checkBlockAvailable(playerData, target, instance)) return true
        if (MatterContainer.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter < playerData.containerCost) return true
        playerData.organicMatter -= playerData.containerCost
        playerData.matterContainers.place(target, instance)
        playerData.matterContainers.select(event.player, playerData.containerCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        val data = gameInstance.playerData[player.uuid.toString()] ?: return
        data.matterContainers.select(player, data.containerCost)
    }
}