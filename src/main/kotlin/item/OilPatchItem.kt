package io.github.flyingpig525.item

import io.github.flyingpig525.building.OilPatch
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item

@Item
object OilPatchItem : BuildingItem<Double>(
    OilPatch,
    BlockData::organicMatter,
    "Organic Matter"
)