package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
import io.github.flyingpig525.building.category.UndergroundCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.ksp.BuildingCompanion
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import kotlin.reflect.KProperty1

@Serializable
class OilPatch : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 2 * count
    override val cost: Int
        get() = genericBuildingCost(count, 750)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }
    override fun onDestruction(point: Point, instance: Instance, data: BlockData): Boolean {
        return !point.buildingPosition.repeatDirection { point, dir ->
            Building.getBuildingByBlock(instance.getBlock(point)) == OilExtractor
        }
    }

    @BuildingCompanion("RockMiner", UndergroundCategory::class, "oilPatches")
    companion object OilPatchCompanion : Building.BuildingCompanion, Validated {
        override val block: Block = Block.BLACK_CARPET
        override val identifier: String = "oil:patch"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::oilPatches

        override fun getItem(cost: Int, count: Int): ItemStack = item(Material.BLACK_CARPET) {
            itemName = "$oilColor$OIL_SYMBOL Oil Patch <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
            lore {
                +"<dark_gray>Brings oil from deep underground".asMini()
                +"<dark_gray>to a more manageable depth".asMini()
                +"<gray>Supplies enough oil to support 4".asMini().noItalic()
                +"<gray>Oil Extractors".asMini().noItalic()
                resourcesConsumed(2, count)
                amountOwned(count)
            }
            set(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: BlockData): ItemStack =
            getItem(playerData.buildings.oilPatches.cost, playerData.buildings.oilPatches.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2
        override fun validate(instance: Instance, point: Point): Boolean {
            return point.isUnderground
        }
    }

}