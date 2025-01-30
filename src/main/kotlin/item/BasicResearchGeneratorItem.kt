package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.BasicResearchGenerator
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object BasicResearchGeneratorItem : Actionable {

    override val identifier: String = "research:basic_research"
    override val itemMaterial: Material = Material.SCULK_SENSOR

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return BasicResearchGenerator.getItem(data)
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.basicResearchStations.select(player, data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementInt(
            event,
            BasicResearchGenerator,
            PlayerData::basicResearchStations,
            PlayerData::mechanicalParts,
            "Mechanical Parts",
            PlayerData::basicResearchStationCost
        )
    }
}