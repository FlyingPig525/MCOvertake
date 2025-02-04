package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterCompressionPlant
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item

@Item
object MatterCompressorItem : BuildingItem<Double>(
    MatterCompressionPlant,
    BlockData::organicMatter,
    "Organic Matter"
)