package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.Barrack
import io.github.flyingpig525.building.TrainingCamp
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.instances
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object TrainingCampItem : Actionable {

    init {
        Actionable.registry += this
        log("${this::class.simpleName} initialized...")

    }

    override val identifier: String = "power:generator"
    override val itemMaterial: Material = TrainingCamp.getItem(1, 1).material()


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()]!!
        return TrainingCamp.getItem(data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacement(
            event,
            TrainingCamp,
            PlayerData::trainingCamps,
            PlayerData::organicMatter,
            PlayerData::trainingCampCost
        )
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.trainingCamps.select(player, data.trainingCampCost)
    }
}