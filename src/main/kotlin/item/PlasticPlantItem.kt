package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.PlasticPlant
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object PlasticPlantItem : Actionable {
    override val identifier: String = "oil:plastic_plant"
    override val itemMaterial: Material = Material.CAMPFIRE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid] ?: return ERROR_ITEM
        return PlasticPlant.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            PlasticPlant,
            PlayerData::organicMatter,
            "Organic Matter"
        )
    }

    override fun setItemSlot(player: Player) {
        player.data?.buildings?.plasticPlants?.select(player)
    }
}