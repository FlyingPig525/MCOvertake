package io.github.flyingpig525.item

import io.github.flyingpig525.building.MatterExtractor
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item

@Item
object MatterExtractorItem : BuildingItem<Double>(
    MatterExtractor,
    BlockData::organicMatter,
    "Organic Matter"
)