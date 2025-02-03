package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.LubricantProcessor
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object LubricantProcessorItem : Actionable {
    override val identifier: String = LubricantProcessor.identifier
    override val itemMaterial: Material = Material.SOUL_CAMPFIRE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return LubricantProcessor.getItem(instance.dataResolver[uuid] ?: return ERROR_ITEM)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            LubricantProcessor,
            BlockData::organicMatter,
            "Organic Matter"
        )
    }

    override fun setItemSlot(player: Player) {
        player.data?.buildings?.lubricantProcessors?.select(player)
    }
}