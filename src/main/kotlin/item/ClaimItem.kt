package io.github.flyingpig525.item

import io.github.flyingpig525.CLAIM_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.players
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

object ClaimItem : Actionable {
    override fun getItem(uuid: UUID): ItemStack {
        val data = players[uuid]!!
        val item = listOf(Material.WOODEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE)[data.claimLevel]
        return item(item) {
            itemName = "<gold>$CLAIM_SYMBOL <bold>Expand</bold> <dark_gray>-<red> $POWER_SYMBOL ${data.claimCost}".asMini()
            amount = 1
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        if (event.itemUseTime > 0) return true
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val data = players[event.player.uuid]!!
        if (instance.getBlock(target) == Block.GRASS_BLOCK && data.power - data.claimCost >= 0) {
            instance.setBlock(target, data.block)
            data.blocks++
            data.power -= data.claimCost
            data.updateBossBars(event.player)
            event.itemUseTime = 15
        } else throw IllegalStateException("Claim target is not grass!!!!!!! wtf is going on!!!! AAAAAAAAAAAAAAAAAAAAAA")
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}