package io.github.flyingpig525.building

import io.github.flyingpig525.building.Building.BuildingCompanion.Companion.registry
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.ksp.PlayerBuildings
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag
import kotlin.reflect.KProperty1

interface Building {
    var count: Int
    val resourceUse: Int
    val cost: Int

    fun place(playerTarget: Point, instance: Instance, data: BlockData)

    /**
     * @return Whether this building should be destroyed or not
     */
    fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean = true
    fun select(player: Player)
    fun tick(data: BlockData) {}

    interface BuildingCompanion {
        val block: Block
        val identifier: String
        val playerRef: KProperty1<PlayerBuildings, Building>

        fun getItem(cost: Int, count: Int): ItemStack
        fun getItem(playerData: BlockData): ItemStack

        fun getResourceUse(currentDisposableResources: Int): Int

        companion object {
            val registry: MutableSet<BuildingCompanion> = mutableSetOf()
        }
    }

    companion object {
        const val ID_TAG = "building_identifier"
        fun blockIsBuilding(block: Block): Boolean {
            val tag = block.getTag(Tag.String(ID_TAG)) ?: return false
            for (entry in registry) {
                if (tag == entry.identifier) return true
            }
            return false
        }

        fun getBuildingByBlock(block: Block): BuildingCompanion? {
            val tag = block.getTag(Tag.String(ID_TAG))
            return registry.find { it.identifier == tag }
        }

        fun getBuildingByIdentifier(id: String): BuildingCompanion? = registry.find { it.identifier == id }

        fun getBuildingIdentifier(block: Block): String? = block.getTag(Tag.String(ID_TAG))

        fun Block.building(identifier: String): Block = withTag(Tag.String(ID_TAG), identifier)

        fun genericBuildingCost(count: Int, cost: Int): Int {
            val generalCost = (count * cost) + cost
            if (generalCost > 10000) {
                return (generalCost/1000) * 1000
            } else if (generalCost > 1000) {
                return (generalCost/100) * 100
            }
            return generalCost
        }
    }
}
