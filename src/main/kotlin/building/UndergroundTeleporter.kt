package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.Building.Companion.genericBuildingCost
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.dsl.blockDisplay
import io.github.flyingpig525.ksp.BuildingCompanion
import io.github.flyingpig525.ksp.PlayerBuildings
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
import java.util.*
import kotlin.reflect.KProperty1

@Serializable
class UndergroundTeleporter : Building, Interactable {
    override var count: Int = 0
    override val resourceUse: Int = count * 20
    override val cost: Int
        get() = genericBuildingCost(count, 750)

    override fun place(playerTarget: Point, instance: Instance, playerData: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        spawn(playerTarget.buildingPosition, instance, playerData.uuid.toUUID()!!)
        count++
    }

    override fun select(player: Player) {
        player.inventory[4] = getItem(cost, count)
    }

    override fun onInteract(e: PlayerBlockInteractEvent): Boolean {
        val data = e.player.data ?: return true
        val pos = e.blockPosition.add(0.5, 0.0, 0.5)
        if (e.instance.getBlock(pos.playerPosition) != data.block) return true
        e.player.teleport(pos.sub(0.0, 9.0, 0.0).asPos())
        data.lastTeleporterPos += pos
        return false
    }

    @BuildingCompanion("BasicResearchGenerator")
    companion object UndergroundTeleporterCompanion : Building.BuildingCompanion, DisplayEntityBlock, Validated {
        override var menuSlot: Int = 6
        override val block: Block = Block.END_GATEWAY
        override val identifier: String = "underground:teleport"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::undergroundTeleporters

        override fun getItem(cost: Int, count: Int): ItemStack {
            return item(Material.COPPER_GRATE) {
                itemName = "<dark_purple>Underground Gateway <gray>-<green> $MATTER_SYMBOL $cost".asMini()
                lore {
                    +"<gray>Allows teleportation to the next underground layer".asMini().noItalic()
                    +"<dark_gray>Can only be placed on rafts".asMini().noItalic()
                    resourcesConsumed(20, count)
                    amountOwned(count)
                }
            }.withTag(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: BlockData): ItemStack =
            getItem(playerData.buildings.undergroundTeleporters.cost, playerData.buildings.undergroundTeleporters.count)

        override fun getResourceUse(currentDisposableResources: Int): Int {
            return currentDisposableResources + 20
        }

        override fun checkShouldSpawn(point: Point, instance: Instance): Boolean =
            instance.getBlock(point.buildingPosition).defaultState() == block
                    && !instance.getNearbyEntities(point.buildingPosition, 0.2).any {
                it is DisplayEntityBlock && (it.entityMeta as BlockDisplayMeta).blockStateId == Block.COPPER_GRATE
            }

        override fun spawn(point: Point, instance: Instance, uuid: UUID) {
            blockDisplay {
                hasGravity = false
                scale = Vec(1.01, 1.01, 1.01)
                translation = Vec(-0.005, -0.005, -0.005)
                block = Block.COPPER_GRATE
                entity {
                    setTag(Tag.UUID("player"), uuid)
                }
            }.setInstance(instance, point)
        }

        override fun validate(instance: Instance, point: Point): Boolean {
            return instance.getBlock(point.visiblePosition).defaultState() == Block.WATER || point.isUnderground
        }
    }
}
