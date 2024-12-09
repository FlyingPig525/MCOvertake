package io.github.flyingpig525.item

import io.github.flyingpig525.building.TrainingCamp
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.log
import io.github.flyingpig525.players
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object TrainingCampItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "power:generator"
    override val itemMaterial: Material = TrainingCamp.getItem(1, 1).material()


    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid.toString()]!!
        return TrainingCamp.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event)
            return true
        }
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = players[event.player.uuid.toString()] ?: return true
        if (!checkBlockAvailable(playerData, target)) return true
        if (TrainingCamp.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter - playerData.trainingCampCost < 0) return true
        playerData.organicMatter -= playerData.trainingCampCost
        playerData.trainingCamps.place(target, instance)
        playerData.trainingCamps.select(event.player, playerData.trainingCampCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()] ?: return
        data.trainingCamps.select(player, data.trainingCampCost)
    }
}