package io.github.flyingpig525.item

import io.github.flyingpig525.building.Barrack
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.item.template.BuildingItem
import io.github.flyingpig525.ksp.Item

@Item
object BarracksItem : BuildingItem<Double>(
    Barrack,
    BlockData::organicMatter,
    "Organic Matter"
)