package io.github.flyingpig525.building

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.MECHANICAL_SYMBOL
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.PlayerData
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

@Serializable
class MatterCompressionPlant : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 4
    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.buildingPosition, block, false)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[4] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) {
        player.inventory[4] = getItem(data)
    }

    override fun tick(data: PlayerData) {
        if (data.organicMatter - 5 * count >= 0) {
            data.organicMatter -= 5 * count
            data.mechanicalParts += count
        }
    }

    companion object MatterCompressionPlantCompanion : Building.BuildingCompanion {
        override val menuSlot: Int = 4
        override val block: Block = Block.HEAVY_CORE
        override val identifier: String = "mechanical:generator"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.HEAVY_CORE) {
                itemName = "<white>$MECHANICAL_SYMBOL Matter Compression Plant <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<dark_gray>Separates and compresses metallic materials found in".asMini()
                    +"<dark_gray>organic matter".asMini()
                    +"<gray>Uses 5 $organicMatter to generate 1 $mechanicalPart".asMini().noItalic()
                    resourcesConsumed(4)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)

            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.matterCompressorCost, playerData.matterCompressors.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2


        init {
            Building.BuildingCompanion.registry += this
            log("${this::class.simpleName} initialized...")
        }
    }
}