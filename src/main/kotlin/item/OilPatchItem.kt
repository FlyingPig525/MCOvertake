package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.OilPatch
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object OilPatchItem : Actionable {
    override val identifier: String = "oil:patch"
    override val itemMaterial: Material = Material.BLACK_CARPET

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid] ?: return ERROR_ITEM
        return OilPatch.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            OilPatch,
            PlayerData::organicMatter,
            "Organic Matter",
            PlayerData::oilPatchCost
        )
    }

    override fun setItemSlot(player: Player) {
        player.data?.oilPatches?.select(player, player.data!!.oilPatchCost)
    }
}