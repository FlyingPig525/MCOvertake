package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.building.UndergroundTeleporter
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.Item
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object UndergroundTeleporterItem : Actionable {
    override val identifier: String = "underground:teleport"
    override val itemMaterial: Material = Material.COPPER_GRATE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val data = instance.dataResolver[uuid.toString()] ?: return ERROR_ITEM
        return UndergroundTeleporter.getItem(data.teleporterCost, data.undergroundTeleporters.count)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        return basicBuildingPlacementDouble(
            event,
            UndergroundTeleporter,
            PlayerData::organicMatter,
            "Organic Matter",
            PlayerData::teleporterCost
        )
    }

    override fun setItemSlot(player: Player) {
        val data = player.data ?: return
        data.undergroundTeleporters.select(player, data)
    }
}