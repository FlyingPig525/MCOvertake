package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.RockMiner
import io.github.flyingpig525.building.UndergroundTeleporter
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object RockMinerItem : Actionable {
    init {
        Actionable.registry += this
    }

    override val identifier: String = "matter:rock_miner"
    override val itemMaterial: Material = Material.TRIPWIRE_HOOK

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid] ?: return ERROR_ITEM
        return RockMiner.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (sneakCheck(event)) return true
        val instance = event.instance
        val target = event.player.getTrueTarget(20) ?: return true
        val data = event.player.data ?: return true
        if (!RockMiner.validate(instance, target)) return true
        if (!checkBlockAvailable(data, target, instance)) return true
        if (data.organicMatter < data.rockMinerCost) return true
        data.organicMatter -= data.rockMinerCost
        data.rockMiners.place(target.buildingPosition, instance, data)
        return true
    }

    override fun setItemSlot(player: Player) {
        player.data?.rockMiners?.select(player, player.data!!.rockMinerCost)
    }
}