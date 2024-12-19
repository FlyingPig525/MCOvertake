package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.Barrack
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.instances
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object BarracksItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "power:container"
    override val itemMaterial: Material = Barrack.getItem(1, 1).material()


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.playerData[uuid.toString()] ?: return ERROR_ITEM
        return Barrack.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event)
            return true
        }
        val instance = event.instance
        val gameInstance = instances.fromInstance(instance) ?: return true
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = gameInstance.playerData[event.player.uuid.toString()] ?: return true
        if (!checkBlockAvailable(playerData, target, instance)) return true
        if (Barrack.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter < playerData.barracksCost) return true
        playerData.organicMatter -= playerData.barracksCost
        playerData.barracks.place(target, instance)
        playerData.barracks.select(event.player, playerData.barracksCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = instances.fromInstance(player.instance)?.playerData?.get(player.uuid.toString()) ?: return
        data.barracks.select(player, data.barracksCost)
    }
}