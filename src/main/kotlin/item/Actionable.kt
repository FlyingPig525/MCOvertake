package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.building.Validated
import io.github.flyingpig525.data.player.PlayerData
import net.bladehunt.kotstom.dsl.item.ItemDsl
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.tag.Tag
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

val ERROR_ITEM = item(Material.BARRIER) {
    itemName = "<red><bold>ERROR".asMini()
}

interface Actionable {
    val identifier: String
    val itemMaterial: Material

    fun getItem(uuid: UUID, instance: GameInstance): ItemStack

    fun onInteract(event: PlayerUseItemEvent): Boolean { return true }
    fun onBreakBlock(event: PlayerBlockBreakEvent): Boolean { return true }
    fun onHandAnimation(event: PlayerHandAnimationEvent) {  }


    fun setItemSlot(player: Player)

    companion object {
        val registry: MutableList<Actionable> = mutableListOf()
        val persistentRegistry: MutableList<Actionable> = mutableListOf()
    }
}

fun claimWithParticle(player: Player, target: Point, resultBlock: Block, instance: Instance) {
    val block = instance.getBlock(target.visiblePosition).defaultState()
    claimWithParticle(player, target, block, resultBlock, instance)
}

fun claimWithParticle(player: Player, target: Point, targetBlock: Block, resultBlock: Block, instance: Instance) {
    instance.setBlock(target.visiblePosition, resultBlock)
    instance.setBlock(target.playerPosition, resultBlock)
    if (player.config?.claimParticles?.value == true) {
        val particle = ParticlePacket(
            Particle.BLOCK.withBlock(targetBlock),
            target.visiblePosition.add(0.5, 1.0, 0.5),
            Vec(0.2, 0.0, 0.2),
            1f,
            30
        )
        player.data?.sendPacket(particle)
    }
}

fun checkBlockAvailable(data: PlayerData, target: Point, instance: Instance): Boolean {
    val playerBlock = instance.getBlock(target.playerPosition).defaultState()
    val buildingBlock = instance.getBlock(target.buildingPosition).defaultState()
    return playerBlock == data.block && (buildingBlock == Block.AIR || buildingBlock == Block.LILY_PAD)
}

fun sneakCheck(event: PlayerUseItemEvent): Boolean {
    if (event.player.isSneaking) {
        SelectBuildingItem.onInteract(event)
        return true
    }
    return false
}

inline fun gameItem(material: Material, identifier: String, fn: @ItemDsl ItemStack.Builder.() -> Unit) = item(material) {
    fn()
    setTag(Tag.String("identifier"), identifier)
}

fun <T : Building> basicBuildingPlacementInt(
    event: PlayerUseItemEvent,
    buildingCompanion: Building.BuildingCompanion,
    buildingRef: KProperty1<PlayerData, T>,
    currencyRef: KMutableProperty1<PlayerData, Int>,
    currencyName: String,
    costRef: KProperty1<PlayerData, Int>
): Boolean {
    if (sneakCheck(event)) return true
    val instance = event.instance
    val target = event.player.getTrueTarget(20) ?: return true
    val playerData = event.player.data ?: return true
    if (!checkBlockAvailable(playerData, target, instance)) return true
    if (buildingCompanion is Validated && !buildingCompanion.validate(event.instance, target.buildingPosition)) return true
    if (buildingCompanion.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) return true
    val cost = costRef.get(playerData)
    val currency = currencyRef.get(playerData)
    if (currency < cost) {
        event.player.sendMessage("<red><bold>Not enough $currencyName </bold>(${currency}/${cost})".asMini())
        return true
    }
    currencyRef.set(playerData, currency - cost)
    val building = buildingRef.get(playerData)
    building.place(target, instance, playerData)
    building.select(event.player, costRef.get(playerData))
    playerData.updateBossBars()
    return true
}

fun <T : Building> basicBuildingPlacementDouble(
    event: PlayerUseItemEvent,
    buildingCompanion: Building.BuildingCompanion,
    buildingRef: KProperty1<PlayerData, T>,
    currencyRef: KMutableProperty1<PlayerData, Double>,
    currencyName: String,
    costRef: KProperty1<PlayerData, Int>
): Boolean {
    if (sneakCheck(event)) return true
    val instance = event.instance
    val target = event.player.getTrueTarget(20) ?: return true
    val playerData = event.player.data ?: return true
    if (!checkBlockAvailable(playerData, target, instance)) return true
    if (buildingCompanion is Validated && !buildingCompanion.validate(event.instance, target.buildingPosition)) return true
    if (buildingCompanion.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) {
        return true
    }
    val cost = costRef.get(playerData)
    val currency = currencyRef.get(playerData)
    if (currency < cost) {
        event.player.sendMessage("<red><bold>Not enough $currencyName </bold>(${currency}/${cost})".asMini())
        return true
    }
    currencyRef.set(playerData, currency - cost)
    val building = buildingRef.get(playerData)
    building.place(target, instance, playerData)
    building.select(event.player, costRef.get(playerData))
    playerData.updateBossBars()
    return true
}