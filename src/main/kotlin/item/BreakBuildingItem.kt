package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.ksp.Item
import io.github.flyingpig525.wall.blockIsWall
import io.github.flyingpig525.wall.wallLevel
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import net.minestom.server.timer.TaskSchedule
import java.util.*

@Item(persistent = true)
object BreakBuildingItem : Actionable {
    override val identifier: String = "building:destroy"
    override val itemMaterial: Material = Material.IRON_PICKAXE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<gold>$PICKAXE_SYMBOL <bold>Destroy Building</bold>".asMini()
            amount = 1
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data = event.player.data ?: return true
        val target = event.player.getTrueTarget(20) ?: return true
        val playerBlockPos = target.playerPosition
        val groundPos = target.visiblePosition
        if (instance.getBlock(playerBlockPos).defaultState() != data.block) return true
        val buildingPos = target.buildingPosition
        val buildingBlock = instance.getBlock(buildingPos)
        val isWater = instance.getBlock(groundPos).defaultState() == Block.WATER
        if (event.player.isSneaking) {
            if (event.player.gameInstance?.removingPlayerBlock?.get(event.player.uuid) == true) {
                if (buildingBlock.defaultState() != Block.LILY_PAD && buildingBlock.defaultState() != Block.AIR) {
                    event.player.sendMessage("<red><bold>Cannot unclaim a block with a building on it".asMini())
                    return true
                }
                instance.setBlock(playerBlockPos, if (isWater) Block.SAND else Block.GRASS_BLOCK)
                if (isWater) {
                    instance.setBlock(buildingPos, Block.AIR)
                    instance.getNearbyEntities(buildingPos, 0.2).onEach {
                        if (it.entityType == EntityType.BLOCK_DISPLAY) {
                            it.remove()
                        }
                    }
                } else {
                    instance.setBlock(groundPos, Block.GRASS_BLOCK)
                }
                data.blocks--
            } else if (event.player.gameInstance?.removingPlayerBlock?.get(event.player.uuid) == null) {
                event.player.sendMessage("<green>Re-attempt to unclaim this block in 1 second to confirm your choice.".asMini())
                event.player.gameInstance?.removingPlayerBlock?.set(event.player.uuid, false)
                instance.scheduler().scheduleTask({
                    event.player.gameInstance?.removingPlayerBlock?.set(event.player.uuid, true)

                }, TaskSchedule.seconds(1), TaskSchedule.stop())
                instance.scheduler().scheduleTask({
                    event.player.gameInstance?.removingPlayerBlock?.remove(event.player.uuid)
                }, TaskSchedule.seconds(10), TaskSchedule.stop())
            }
            return true
        }
        if (buildingBlock.defaultState() == Block.LILY_PAD) return true
        val identifier = Building.getBuildingIdentifier(buildingBlock)
        if (identifier == null) {
            if (!blockIsWall(buildingBlock)) return true
        } else {
            val ref = Building.getBuildingByIdentifier(identifier)?.playerRef?.get(data.buildings) ?: return true
            if (!ref.onDestruction(buildingPos, instance, data)) return true
            ref.count--
            ref.select(event.player)
            ActionData.DestroyBuilding(data, instance, event.player).apply {
                building = ref
                wallLevel = buildingBlock.wallLevel
            }.also { data.research.onDestroyBuilding(it) }
        }
        if (instance.getBlock(groundPos) == Block.WATER) {
            instance.setBlock(buildingPos, Block.LILY_PAD)
        } else {
            instance.setBlock(buildingPos, Block.AIR)
            buildingPos.repeatAdjacent {
                UpgradeWallItem.updateWall(it, instance)
            }
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[5] = getItem(player.uuid, instances.fromInstance(player.instance) ?: return)
    }
}