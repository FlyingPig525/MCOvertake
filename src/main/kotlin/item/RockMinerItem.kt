package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.RockMiner
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
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
            BlockData::mechanicalParts,
            "Mechanical Parts"
        )
    }

    override fun setItemSlot(player: Player) {
        player.data?.buildings?.rockMiners?.select(player)
    }
}