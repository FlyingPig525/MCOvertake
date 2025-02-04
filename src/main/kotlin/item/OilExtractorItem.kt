package io.github.flyingpig525.item

import io.github.flyingpig525.building.OilExtractor
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item

@Item
object OilExtractorItem : BuildingItem<Double>(
    OilExtractor,
    BlockData::organicMatter,
    "Organic Matter"
)