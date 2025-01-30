package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.RockMiner
import io.github.flyingpig525.building.UndergroundTeleporter
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.ServerPacket.Play
import java.util.*

@Item
object RockMinerItem : Actionable {

    override val identifier: String = "matter:rock_miner"
    override val itemMaterial: Material = Material.TRIPWIRE_HOOK

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid] ?: return ERROR_ITEM
        return RockMiner.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementInt(
            event,
            RockMiner,
            PlayerData::rockMiners,
            PlayerData::mechanicalParts,
            "Mechanical Parts",
            PlayerData::rockMinerCost
        )
    }

    override fun setItemSlot(player: Player) {
        player.data?.rockMiners?.select(player, player.data!!.rockMinerCost)
    }
}