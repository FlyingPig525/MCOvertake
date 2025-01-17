package io.github.flyingpig525.building

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.data.research.currency.BasicResearch
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
class BasicResearchGenerator : Building {
    override var count: Int = 0
    override val resourceUse: Int = 3

    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.buildingPosition, block)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) {
        select(player, data.basicResearchStationCost)
    }

    override fun tick(data: PlayerData) {
        if (data.power >= 2 && data.mechanicalParts >= 3) {
            data.power -= 2
            data.mechanicalParts -= 3
            data.research.basicResearch.count++
        }
    }

    companion object BasicResearchGeneratorCompanion : Building.BuildingCompanion {
        override val menuSlot: Int = 7
        override val block: Block = Block.SCULK_SENSOR
        override val identifier: String = "research:basic_research"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.SCULK_SENSOR) {
                itemName = "<${BasicResearch.color}>${BasicResearch.symbol} Basic Research Station <gray>-</gray><green> $MECHANICAL_SYMBOL $cost".asMini()
                lore {
                    +"<dark_gray>Employs scientists to aid development of new tech".asMini()
                    +"<dark_gray>using mechanical parts"
                    +"<gray>Consumes 2 $power and 3 $mechanicalPart to generate 1 basic research".asMini().noItalic()
                    resourcesConsumed(3, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.basicResearchStationCost, playerData.basicResearchStations.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 3
        init {
            Building.BuildingCompanion.registry += this
            log("${this::class.simpleName} initialized...")
        }
    }
}