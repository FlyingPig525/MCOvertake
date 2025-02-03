package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.OilExtractor
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object OilExtractorItem : Actionable {
    override val identifier: String = "oil:extractor"
    override val itemMaterial: Material = Material.BLACK_CANDLE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid] ?: return ERROR_ITEM
        return OilExtractor.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            OilExtractor,
            BlockData::organicMatter,
            "Organic Matter"
        )
    }

    override fun setItemSlot(player: Player) {
        player.data?.buildings?.oilExtractors?.select(player)
    }
}