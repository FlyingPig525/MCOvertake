package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.*
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.data.PlayerData.Companion.getDataByBlock
import io.github.flyingpig525.data.PlayerData.Companion.getDataByPoint
import io.github.flyingpig525.wall.blockIsWall
import io.github.flyingpig525.wall.getWallAttackCost
import io.github.flyingpig525.wall.lastWall
import io.github.flyingpig525.wall.wallLevel
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*

object AttackItem : Actionable {

    init {
        Actionable.registry += this
    }

    override val identifier: String = "block:attack"

    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.DIAMOND_SWORD) {
            val player = instance.getPlayerByUuid(uuid) ?: return ERROR_ITEM
            val target = player.getTrueTarget(20) ?: return ERROR_ITEM
            val targetData = getAttacking(player)
            val targetName = targetData?.playerDisplayName ?: ""
            val attackCost = getAttackCost(targetData, target)

            itemName = "<red>$ATTACK_SYMBOL <bold>Attack $targetName</bold> <gray>- <red>$POWER_SYMBOL <bold>$attackCost".asMini().asComponent()
            set(Tag.String("identifier"), identifier)
        }
    }

    private fun getAttacking(player: Player): PlayerData? {
        val target = player.getTrueTarget(20)!!
        // TODO: AFTER ADDING WALLS ADD WALL THINGS HERE
        return players.getDataByPoint(target)
    }

    private fun getAttackCost(player: Player): Int {
        val target = player.getTrueTarget(20)!!
        val targetData = getAttacking(player)
        val targetAttackCost = getAttackCost(targetData, target)
        return targetAttackCost
    }

    private fun getAttackCost(targetData: PlayerData?, playerTarget: Point): Int {
        // TODO: AFTER ADDING WALLS ADD WALL THINGS HERE
        val wallPosition = if (playerTarget.blockY() == 39) playerTarget.add(0.0, 1.0, 0.0) else playerTarget
        val building = instance.getBlock(wallPosition)
        var additiveModifier = 0
        if (blockIsWall(building)) additiveModifier += getWallAttackCost(building)!!
        return (targetData?.baseAttackCost ?: 15) + additiveModifier
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        // TODO: ADD COOLDOWN
        val target = event.player.getTrueTarget(20) ?: return true
        val buildingPoint = if (target.blockY() == 40) target else target.add(0.0, 1.0, 0.0)
        val playerBlock = instance.getBlock(if (target.blockY() == 39) target else target.sub(0.0, 1.0, 0.0))
        val buildingBlock = instance.getBlock(buildingPoint)
        val data = players[event.player.uuid.toString()] ?: return true
        if (playerBlock == Block.GRASS_BLOCK || playerBlock == data.block) {
            return true
        }
        val attackCost = getAttackCost(event.player)
        if (data.power < attackCost) {
            // TODO: ADD MESSAGE
            return true
        }
        val targetData = getAttacking(event.player) ?: return true
        val targetPlayer = instance.getPlayerByUuid(targetData.uuid.toUUID())
        val taken = when(buildingBlock) {
            Block.AIR -> { true }
            Barrack.block -> {
                targetData.barracks.count--
                data.barracks.count++
                true
            }
            MatterContainer.block -> {
                targetData.matterContainers.count--
                data.matterContainers.count++
                true
            }
            MatterExtractor.block -> {
                targetData.matterExtractors.count--
                data.matterExtractors.count++
                true
            }
            TrainingCamp.block -> {
                targetData.trainingCamps.count--
                data.trainingCamps.count++
                true
            }
            else -> {
                // TODO: ADD PARTICLES
                val wallLevel = buildingBlock.wallLevel ?: return true
                if (wallLevel == 1) {
                    instance.setBlock(buildingPoint, Block.AIR)
                    // PARTICLES
                } else {
                    instance.setBlock(buildingPoint, lastWall(wallLevel))
                    UpgradeWallItem.updateWall(buildingPoint)
                    // PARTICLES
                }
                repeatAdjacent(buildingPoint) { UpgradeWallItem.updateWall(it) }
                false
            }
        }
        if (taken) {
            data.blocks++
            targetData.blocks--
            if (Building.blockIsBuilding(buildingBlock)) {
                claimWithParticle(event.player, target.sub(0.0, 1.0, 0.0), data.block)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                SelectBuildingItem.updatePlayerItem(event.player)
            } else {
                claimWithParticle(event.player, target, data.block)
            }
        }
        data.power -= attackCost
        data.updateBossBars()
        targetData.updateBossBars()

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}