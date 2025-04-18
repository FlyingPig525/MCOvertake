package io.github.flyingpig525.building

import io.github.flyingpig525.BUILDING_INVENTORY_SLOT
import io.github.flyingpig525.building.Building.BuildingCompanion.Companion.registry
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag
import kotlin.reflect.KProperty1

@Serializable
abstract class Building(val producesPollution: Boolean = false) {
    var count: Int = 0
    abstract val resourceUse: Int
    abstract val cost: CurrencyCost
    protected abstract val itemGetter: (cost: CurrencyCost, count: Int) -> ItemStack

    abstract fun place(playerTarget: Point, instance: Instance, data: BlockData)

    /**
     * @return Whether this building should be destroyed or not
     */
    open fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean = true
    open fun select(player: Player) {
        val item = itemGetter(cost, count)
        val lore = item.get(ItemComponent.LORE).apply { this?.add("<gold>Crouch + right-click to select another building".asMini()) }
        player.inventory[BUILDING_INVENTORY_SLOT] = if (lore != null) item.withLore(lore) else item
    }
    open fun tick(data: BlockData) {}

    override fun toString(): String {
        return "${this::class.simpleName}[count=$count, resourceUse=$resourceUse, cost=$cost]"
    }

    interface BuildingCompanion {
        val block: Block
        val identifier: String
        val playerRef: KProperty1<PlayerBuildings, Building>

        fun getItem(cost: CurrencyCost, count: Int): ItemStack
        fun getItem(playerData: BlockData): ItemStack {
            val building = playerRef.get(playerData.buildings)
            return getItem(building.cost, building.count)
        }

        /**
         * @param [currentDisposableResources] The current total
         * @param [count] The amount of this building the player owns
         *
         * @return The amount of disposable resources another one of this building would make the total be
         */
        fun getResourceUse(currentDisposableResources: Int, count: Int): Int

        fun shouldCallItemUse(item: ItemStack): Boolean = false

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
