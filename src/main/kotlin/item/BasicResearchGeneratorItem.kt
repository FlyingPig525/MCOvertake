package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.BasicResearchGenerator
import io.github.flyingpig525.data
import io.github.flyingpig525.getTrueTarget
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object BasicResearchGeneratorItem : Actionable {
    init {
        Actionable.registry += this
    }

    override val identifier: String = "research:basic_research"
    override val itemMaterial: Material = Material.SCULK_SENSOR

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.playerData[uuid.toString()] ?: return ERROR_ITEM
        return BasicResearchGenerator.getItem(data)
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.basicResearchCategory.basicResearchStations.select(player, data)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val target = event.player.getTrueTarget(20) ?: return true
        val data = event.player.data ?: return true
        data.basicResearchCategory.basicResearchStations.place(target, event.instance)
        return true
    }
}