package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.MatterContainer
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object MatterContainerItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }
    override val identifier: String = "matter:container"
    override val itemMaterial: Material = MatterContainer.getItem(1, 1).material()

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return MatterContainer.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            MatterContainer,
            PlayerData::matterContainers,
            PlayerData::organicMatter,
            PlayerData::matterContainerCost
        )
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.matterContainers.select(player, data.matterContainerCost)
    }
}