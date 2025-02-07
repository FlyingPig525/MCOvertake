package io.github.flyingpig525.building.category

import io.github.flyingpig525.building.Building
import net.minestom.server.item.ItemStack

open class BuildingCategory {
    open val icon: ItemStack = ItemStack.AIR
    val buildings: MutableList<Building.BuildingCompanion> = mutableListOf()
}