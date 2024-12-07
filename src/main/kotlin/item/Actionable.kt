package io.github.flyingpig525.item

import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.instance
import io.github.flyingpig525.playerPosition
import io.github.flyingpig525.visiblePosition
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import java.util.*

val ERROR_ITEM = item(Material.BARRIER) {
    itemName = "<red><bold>ERROR".asMini()
}

interface Actionable {
    val identifier: String
    val itemMaterial: Material

    fun getItem(uuid: UUID): ItemStack

    fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean { return true }
    fun onBreakBlock(event: PlayerBlockBreakEvent, instance: Instance): Boolean { return true }


    fun setItemSlot(player: Player)

    companion object {
        val registry: MutableList<Actionable> = mutableListOf()
        val persistentRegistry: MutableList<Actionable> = mutableListOf()
    }
}

fun claimWithParticle(player: Player, target: Point, resultBlock: Block) {
    val block = instance.getBlock(target.playerPosition).defaultState()
    claimWithParticle(player, target, block, resultBlock)
}

fun claimWithParticle(player: Player, target: Point, targetBlock: Block, resultBlock: Block) {
    instance.setBlock(target.visiblePosition, resultBlock)
    instance.setBlock(target.playerPosition, resultBlock)
    // TODO: FIX CLAIM PARTICLES (likely minestom problem)
    val particle = ParticlePacket(
        Particle.BLOCK.withBlock(targetBlock),
        target.visiblePosition.add(0.5, 1.0, 0.5),
        Vec(0.2, 0.0, 0.2),
        1f,
        30
    )
    player.sendPacket(particle)
}

fun checkBlockAvailable(data: PlayerData, target: Point): Boolean {
    val playerBlock = instance.getBlock(target.playerPosition).defaultState()
    val buildingBlock = instance.getBlock(target.buildingPosition).defaultState()
    return playerBlock == data.block && (buildingBlock == Block.AIR || buildingBlock == Block.LILY_PAD)
}
