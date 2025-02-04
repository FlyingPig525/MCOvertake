package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterContainer
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item

@Item
object MatterContainerItem : BuildingItem<Double>(
    MatterContainer,
    BlockData::organicMatter,
    "Organic Matter"
)