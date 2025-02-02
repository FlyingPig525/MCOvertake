package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.RockMiner.RockMinerCompanion
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.ksp.BuildingCompanion
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

    override fun place(playerTarget: Point, instance: Instance, data: PlayerData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) = select(player, data.oilPatchCost)

    override fun onDestruction(point: Point, instance: Instance, data: PlayerData): Boolean {
        return !point.buildingPosition.repeatDirection { point, dir ->
            Building.getBuildingByBlock(instance.getBlock(point)) == OilExtractor
        }
    }

    @BuildingCompanion("RockMiner")
    companion object OilPatchCompanion : Building.BuildingCompanion, Validated {
        override var menuSlot: Int = 0
        override val block: Block = Block.BLACK_CARPET
        override val identifier: String = "oil:patch"
        override val playerRef: KProperty1<PlayerData, Building> = PlayerData::oilPatches

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

        override fun getItem(playerData: PlayerData): ItemStack =
            getItem(playerData.oilPatchCost, playerData.oilPatches.count)

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2
        override fun validate(instance: Instance, point: Point): Boolean {
            return point.isUnderground
        }
    }

}