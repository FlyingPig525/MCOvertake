package io.github.flyingpig525.building

import io.github.flyingpig525.BUILDING_INVENTORY_SLOT
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.category.BasicCategory
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
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
class Barrack : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 2
    override val cost get() = CurrencyCost.genericOrganicMatter(count, 25.0)
    override fun place(playerTarget: Point, instance: Instance, playerData: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    @BuildingCompanion("TrainingCamp", BasicCategory::class)
    companion object BarrackCompanion : Building.BuildingCompanion {
        override val block: Block = Block.SOUL_LANTERN
        override val identifier: String = "power:container"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::barracks

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.SOUL_LANTERN) {
                itemName = "<red>$POWER_SYMBOL Barracks</red> <gray>-</gray><green> $MATTER_SYMBOL ${cost.organicMatter}".asMini()
                lore {
                    +"<dark_gray>Provides area to store powerful assets"
                    +"<gray>Increases Max $power Storage".asMini().noItalic()
                    resourcesConsumed(2, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack {
            return getItem(playerData.buildings.barracks.cost, playerData.buildings.barracks.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2
    }
}