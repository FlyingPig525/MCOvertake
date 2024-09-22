package io.github.flyingpig525.item

import io.github.flyingpig525.COLONY_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.players
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.UUID

object ColonyItem : Actionable {
    override fun getItem(uuid: UUID): ItemStack {
        val playerData = players[uuid]
        return item(Material.CHEST) {
            itemName = "<green>$COLONY_SYMBOL Instantiate Colony <dark_gray>-<red> $POWER_SYMBOL ${playerData?.colonyCost}".asMini()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.itemUseTime > 0) return true
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val data = players[event.player.uuid]!!
        if (instance.getBlock(target) == Block.GRASS_BLOCK && data.power - data.colonyCost >= 0) {
            instance.setBlock(target, data.block)
            data.blocks++
            data.power -= data.colonyCost
            event.itemUseTime = 300
            data.updateBossBars(event.player)
        } else throw IllegalStateException("Colony target is not grass!!!!!!! wtf is going on!!!! AAAAAAAAAAAAAAAAAAAAAA")
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}