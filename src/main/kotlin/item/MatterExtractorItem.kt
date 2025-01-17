package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.MatterExtractor
import io.github.flyingpig525.data
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.instances
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object MatterExtractorItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }
    override val identifier: String = "matter:generator"
    override val itemMaterial: Material = MatterExtractor.getItem(1, 1).material()

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return MatterExtractor.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (sneakCheck(event)) return true
        val instance = event.instance
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = event.player.data ?: return true
        if (!checkBlockAvailable(playerData, target, instance)) return true
        if (MatterExtractor.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
        if (playerData.organicMatter < playerData.extractorCost) return true
        playerData.organicMatter -= playerData.extractorCost
        playerData.matterExtractors.place(target, instance)
        playerData.matterExtractors.select(event.player, playerData.extractorCost)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = player.data!!
        data.matterExtractors.select(player, data.extractorCost)
    }
}