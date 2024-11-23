package io.github.flyingpig525.building

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
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

@Serializable
class Barrack : Building {

    override var count: Int = 0
    override val resourceUse: Int get() = getResourceUse(count)

    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.add(0.0, 1.0, 0.0), block, false)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[4] = getItem(cost, count)
    }

    companion object : Building.BuildingCompanion {
        override val block: Block = Block.SOUL_LANTERN
        override val identifier: String = "power:container"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.SOUL_LANTERN) {
                itemName = "<red>$POWER_SYMBOL Barracks</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<gray>Increases Max $power Storage".asMini().noItalic()
                    resourcesConsumed(2)
                    amountOwned(count)
                }
            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.barracksCost, playerData.barracks.count)
        }

        override fun getResourceUse(count: Int): Int = count * 2

        init {
            Building.BuildingCompanion.registry += this
        }
    }
}