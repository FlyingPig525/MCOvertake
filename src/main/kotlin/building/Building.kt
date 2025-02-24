package io.github.flyingpig525.building

import io.github.flyingpig525.building.Building.BuildingCompanion.Companion.registry
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag
import kotlin.reflect.KProperty1

@Serializable
abstract class Building {
    var count: Int = 0
    abstract val resourceUse: Int
    abstract val cost: CurrencyCost

    abstract fun place(playerTarget: Point, instance: Instance, data: BlockData)

    /**
     * @return Whether this building should be destroyed or not
     */
    open fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean = true
    abstract fun select(player: Player)
    open fun tick(data: BlockData) {}

    override fun toString(): String {
        return "${this::class.simpleName}[count=$count, resourceUse=$resourceUse, cost=$cost]"
    }

    interface BuildingCompanion {
        val block: Block
        val identifier: String
        val playerRef: KProperty1<PlayerBuildings, Building>

        fun getItem(cost: CurrencyCost, count: Int): ItemStack
        fun getItem(playerData: BlockData): ItemStack

        fun getResourceUse(currentDisposableResources: Int, count: Int): Int

        fun shouldCallItemUse(): Boolean = false

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
    }
}
