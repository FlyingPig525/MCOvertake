package io.github.flyingpig525.item

import io.github.flyingpig525.instance
import net.bladehunt.kotstom.dsl.particle
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.particle.Particle
import java.util.UUID

interface Actionable {
    fun getItem(uuid: UUID): ItemStack

    fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean { return true }
    fun onBreakBlock(event: PlayerBlockBreakEvent, instance: Instance): Boolean { return true }


    fun setItemSlot(player: Player)

    companion object {
        val registry: MutableList<Actionable> = mutableListOf()
    }
}

fun claimWithParticle(player: Player, target: Point, resultBlock: Block) {
    val block = instance.getBlock(target)
    claimWithParticle(player, target, block, resultBlock)
}

fun claimWithParticle(player: Player, target: Point, targetBlock: Block, resultBlock: Block) {
    instance.setBlock(target, resultBlock)
    val particle = particle {
        particle = Particle.BLOCK.withBlock(targetBlock)
        count = 30
        position = target.add(0.5, 1.0, 0.5)
        this.offset = Vec(0.2, 0.0, 0.2)
    }
    player.sendPacket(particle)
}