package io.github.flyingpig525.item

import io.github.flyingpig525.building.UndergroundTeleporter
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item
import net.minestom.server.item.Material

@Item
object UndergroundTeleporterItem : BuildingItem(
    UndergroundTeleporter,
    Material.COPPER_GRATE
)