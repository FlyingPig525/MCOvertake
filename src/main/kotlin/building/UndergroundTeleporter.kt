package io.github.flyingpig525.building

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.dsl.blockDisplay
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.asPos
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.BlockDisplayMeta
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag

@Serializable
class UndergroundTeleporter : Building, Interactable {
    override var count: Int = 0
    override val resourceUse: Int = count * 20
    override fun place(playerTarget: Point, instance: Instance) {
        instance.setBlock(playerTarget.buildingPosition, block)
        spawn(playerTarget.buildingPosition, instance)
        count++
    }

    override fun select(player: Player, cost: Int) {
        player.inventory[4] = getItem(cost, count)
    }

    override fun select(player: Player, data: PlayerData) {
        select(player, data.teleporterCost)
    }

    override fun onInteract(e: PlayerBlockInteractEvent): Boolean {
        val gameInstance = instances.fromInstance(e.instance) ?: return true
        val data = gameInstance.playerData[e.player.uuid.toString()] ?: return true
        val pos = e.blockPosition.add(0.5, 0.0, 0.5)
        if (e.instance.getBlock(pos.playerPosition) != data.block) return true
        e.player.teleport(pos.sub(0.0, 9.0, 0.0).asPos())
        data.lastTeleporterPos += pos
        return false
    }

    companion object UndergroundTeleporterCompanion : Building.BuildingCompanion, DisplayEntityBlock {
        override val menuSlot: Int = 5
        init {
            Building.BuildingCompanion.registry += this
            log("${this::class.simpleName} initialized...")
        }
        override val block: Block = Block.END_GATEWAY
        override val identifier: String = "underground:teleport"

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.COPPER_GRATE) {
                itemName = "<dark_purple>Underground Gateway <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<gray>Allows teleportation to the next underground layer".asMini().noItalic()
                    resourcesConsumed(20)
                    amountOwned(count)
                }
            }.withTag(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: PlayerData): ItemStack {
            return getItem(playerData.teleporterCost, playerData.undergroundTeleporters.count)
        }

        override fun getResourceUse(currentDisposableResources: Int): Int {
            return currentDisposableResources + 20
        }

        override fun checkShouldSpawn(point: Point, instance: Instance): Boolean =
            instance.getBlock(point.buildingPosition).defaultState() == block
                    && !instance.getNearbyEntities(point.buildingPosition, 0.2).any {
                it is DisplayEntityBlock && (it.entityMeta as BlockDisplayMeta).blockStateId == Block.COPPER_GRATE
            }

        override fun spawn(point: Point, instance: Instance) {
            blockDisplay {
                hasGravity = false
                scale = Vec(1.01, 1.01, 1.01)
                translation = Vec(-0.005, -0.005, -0.005)
                block = Block.COPPER_GRATE
            }.setInstance(instance, point)
        }
    }
}