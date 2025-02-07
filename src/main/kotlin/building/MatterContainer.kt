package io.github.flyingpig525.building

import io.github.flyingpig525.BUILDING_INVENTORY_SLOT
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
import io.github.flyingpig525.building.category.BasicCategory
import io.github.flyingpig525.buildingPosition
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
class MatterContainer : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 2
    override val cost: Int
        get() = genericBuildingCost(count, 20)

    override fun place(playerTarget: Point, instance: Instance, playerData: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    @BuildingCompanion("MatterExtractor", BasicCategory::class)
    companion object MatterContainerCompanion : Building.BuildingCompanion {
        override val block: Block = Block.LANTERN
        override val identifier: String = "matter:container"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::matterContainers

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.LANTERN) {
                itemName = "<green>$MATTER_SYMBOL Organic Matter Container <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<dark_gray>Stores 25 cubic feet of organic matter".asMini()
                    +"<gray>Increases Max $organicMatter Storage".asMini().noItalic()
                    resourcesConsumed(2, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)

            }
        }

        override fun getItem(playerData: BlockData): ItemStack {
            return getItem(playerData.buildings.matterContainers.cost, playerData.buildings.matterContainers.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2
    }
}