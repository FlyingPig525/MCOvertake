package io.github.flyingpig525.item

import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import java.util.UUID

interface Actionable {
    fun getItem(uuid: UUID): ItemStack

    fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean { return true }
    fun onBreakBlock(event: PlayerBlockBreakEvent, instance: Instance): Boolean { return true }


    fun setItemSlot(player: Player)
}