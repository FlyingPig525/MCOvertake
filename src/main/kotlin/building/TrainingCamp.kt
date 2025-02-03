package io.github.flyingpig525.building

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.player.PlayerData
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
class TrainingCamp : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 3
    override val cost: Int
        get() = genericBuildingCost(count, 25)

    override fun place(playerTarget: Point, instance: Instance, playerData: PlayerData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun select(player: Player) {
        player.inventory[4] = getItem(cost, count)
    }

    override fun tick(data: PlayerData) {
        data.power += count * 0.5 + 0.5
    }

    @BuildingCompanion("first")
    companion object TrainingCampCompanion : Building.BuildingCompanion {
        override var menuSlot: Int = 1
        override val block: Block = Block.POLISHED_BLACKSTONE_BUTTON.withProperty("face", "floor")
        override val identifier: String = "power:generator"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::trainingCamps

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.POLISHED_BLACKSTONE_BUTTON) {
                itemName = "<red>$POWER_SYMBOL Training Camp</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<dark_gray>Provides space for troops and other assets to".asMini()
                    +"<dark_gray>refine their specific skills".asMini()
                    +"<gray>Generates 0.5 $power every 70 ticks".asMini().noItalic()
                    resourcesConsumed(3, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.buildings.trainingCamps.cost, playerData.buildings.trainingCamps.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3
    }
}