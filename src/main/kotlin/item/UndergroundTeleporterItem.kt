package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.UndergroundTeleporter
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object UndergroundTeleporterItem : Actionable {
    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")
    }

    override val identifier: String = "underground:teleport"
    override val itemMaterial: Material = Material.COPPER_GRATE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.playerData[uuid.toString()] ?: return ERROR_ITEM
        return UndergroundTeleporter.getItem(data.teleporterCost, data.undergroundTeleporters.count)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event)
            return true
        }
        val gameInstance = instances.fromInstance(event.instance) ?: return true
        val instance = event.instance
        val target = event.player.getTrueTarget(20) ?: return true
        val data = gameInstance.playerData[event.player.uuid.toString()] ?: return true
        log("data")
        if (!target.isUnderground && instance.getBlock(target.visiblePosition).defaultState() != Block.WATER) return true
        log("allowed")
        if (!checkBlockAvailable(data, target, instance)) return true
        log("available")
        if (data.organicMatter < data.teleporterCost) return true
        log("cost")
        data.organicMatter -= data.teleporterCost
        data.undergroundTeleporters.place(target.buildingPosition, instance)
        return true
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        val data = gameInstance.playerData[player.uuid.toString()] ?: return
        data.undergroundTeleporters.select(player, data)
    }
}