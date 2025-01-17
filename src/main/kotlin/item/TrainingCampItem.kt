package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.TrainingCamp
import io.github.flyingpig525.data
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.instances
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


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()]!!
        return TrainingCamp.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (sneakCheck(event)) return true
        val instance = event.instance
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = event.player.data ?: return true
        if (!checkBlockAvailable(playerData, target, instance)) return true
        if (TrainingCamp.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter < playerData.trainingCampCost) return true
        playerData.organicMatter -= playerData.trainingCampCost
        playerData.trainingCamps.place(target, instance)
        playerData.trainingCamps.select(event.player, playerData.trainingCampCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.trainingCamps.select(player, data.trainingCampCost)
    }
}