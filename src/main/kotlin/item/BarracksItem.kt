package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.Barrack
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object BarracksItem : Actionable {

    override val identifier: String = "power:container"
    override val itemMaterial: Material = Barrack.getItem(1, 1).material()


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return Barrack.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            Barrack,
            PlayerData::barracks,
            PlayerData::organicMatter,
            PlayerData::barracksCost
        )
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.barracks.select(player, data.barracksCost)
    }
}