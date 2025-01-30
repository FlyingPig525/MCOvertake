package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.MatterCompressionPlant
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object MatterCompressorItem : Actionable {
    override val identifier: String = "mechanical:generator"
    override val itemMaterial: Material = MatterCompressionPlant.getItem(1, 1).material()

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return MatterCompressionPlant.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            MatterCompressionPlant,
            PlayerData::matterCompressors,
            PlayerData::organicMatter,
            "Organic Matter",
            PlayerData::matterCompressorCost
        )
    }

    override fun setItemSlot(player: Player) {
        val data = player.data!!
        data.matterCompressors.select(player, data.matterExtractorCost)
    }
}