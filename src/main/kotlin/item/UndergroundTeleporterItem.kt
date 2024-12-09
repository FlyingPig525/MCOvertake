package io.github.flyingpig525.item

import io.github.flyingpig525.*
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

    override fun getItem(uuid: UUID): ItemStack {
        return UndergroundTeleporter.getItem(1, 1)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (event.player.isSneaking) {
            SelectBuildingItem.onInteract(event)
            return true
        }
        val instance = event.instance
        val target = event.player.getTrueTarget(20) ?: return true
        val data = players[event.player.uuid.toString()] ?: return true
        if (instance.getBlock(target.visiblePosition).defaultState() != Block.WATER || !target.isUnderground) return true
        if (checkBlockAvailable(data, target)) return true
        if (data.organicMatter - data.teleporterCost < 0) return true
        data.organicMatter -= data.teleporterCost
        data.undergroundTeleporters.place(target.buildingPosition, instance)
        return true
    }

    override fun setItemSlot(player: Player) {
        val data = players[player.uuid.toString()] ?: return
        data.undergroundTeleporters.select(player, data)
    }
}