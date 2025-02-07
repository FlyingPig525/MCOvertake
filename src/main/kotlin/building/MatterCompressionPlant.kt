package io.github.flyingpig525.building

import io.github.flyingpig525.BUILDING_INVENTORY_SLOT
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.MECHANICAL_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
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
class MatterCompressionPlant : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 4
    override val cost: Int
        get() = (count * 50) + 50

    override fun place(playerTarget: Point, instance: Instance, playerData: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun tick(data: BlockData) {
        if (!data.blockConfig.doIntermediary.value) return
        if (data.organicMatter >= 5 * count) {
            data.organicMatter -= 5 * count
            data.mechanicalParts += count
        }
    }

    @BuildingCompanion("MatterContainer", BasicCategory::class, "matterCompressors")
    companion object MatterCompressionPlantCompanion : Building.BuildingCompanion {
        override val block: Block = Block.HEAVY_CORE
        override val identifier: String = "mechanical:generator"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::matterCompressors

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.HEAVY_CORE) {
                itemName = "<white>$MECHANICAL_SYMBOL Matter Compression Plant <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<dark_gray>Separates and compresses metallic materials found in".asMini()
                    +"<dark_gray>organic matter".asMini()
                    +"<gray>Uses 5 $organicMatter to generate 1 $mechanicalPart".asMini().noItalic()
                    resourcesConsumed(4, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)

            }
        }

        override fun getItem(playerData: BlockData): ItemStack {
            return getItem(playerData.buildings.matterCompressors.cost, playerData.buildings.matterCompressors.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2
    }
}