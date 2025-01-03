package io.github.flyingpig525.building

import cz.lukynka.prettylog.log
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.player.PlayerData
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
class Barrack : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = count * 2
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

    companion object BarrackCompanion : Building.BuildingCompanion {
        override val menuSlot: Int = 3
        override val block: Block = Block.SOUL_LANTERN
        override val identifier: String = "power:container"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.SOUL_LANTERN) {
                itemName = "<red>$POWER_SYMBOL Barracks</red> <gray>-</gray><green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<dark_gray>Provides area to store powerful assets"
                    +"<gray>Increases Max $power Storage".asMini().noItalic()
                    resourcesConsumed(2)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)

            }
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.barracksCost, playerData.barracks.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int = currentDisposableResources + 2


        init {
            Building.BuildingCompanion.registry += this
            log("${this::class.simpleName} initialized...")
        }
    }
}