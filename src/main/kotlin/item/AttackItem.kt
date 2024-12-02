package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.*
import io.github.flyingpig525.data.PlayerData
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
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.tag.Tag
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

object AttackItem : Actionable {

    init {
        Actionable.registry += this
    }

    override val identifier: String = "block:attack"
    override val itemMaterial: Material = Material.DIAMOND_SWORD

    override fun getItem(uuid: UUID): ItemStack {
        return item(itemMaterial) {
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

    private fun getAttackCost(targetData: PlayerData?, playerTarget: Point): Int {
        // TODO: AFTER ADDING WALLS ADD WALL THINGS HERE
        val wallPosition = playerTarget.buildingPosition
        val building = instance.getBlock(wallPosition)
        var additiveModifier = 0
        if (blockIsWall(building)) additiveModifier += getWallAttackCost(building)!!
        return (targetData?.baseAttackCost ?: 15) + additiveModifier
    }

    private fun getAttackCooldown(targetData: PlayerData?, wallLevel: Int): Cooldown {
        // TODO: When research-like things get implemented, add them
        var cooldownTicks = 20L
        if (wallLevel <= 10) {
            cooldownTicks += 3 * wallLevel
        } else {
            cooldownTicks += (wallLevel * wallLevel) / 3
        }
        return Cooldown(Duration.ofMillis(cooldownTicks*50))
    }

    private fun attackRaft(targetData: PlayerData, point: Point) {
        ClaimWaterItem.destroyPlayerRaft(point.withY(40.0))
        targetData.blocks--
        instance.setBlock(point.withY(38.0), Block.SAND)
        instance.setBlock(point.buildingPosition, Block.AIR)
        // TODO: PARTICLES
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()] ?: return true
        if (!data.attackCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        val buildingPoint = target.buildingPosition
        val playerBlock = instance.getBlock(target.playerPosition)
        val waterBlock = instance.getBlock(target.withY(39.0))
        val buildingBlock = instance.getBlock(buildingPoint)
        if (playerBlock == Block.GRASS_BLOCK || playerBlock == Block.SAND || playerBlock == data.block) {
            return true
        }
        val targetData = getAttacking(event.player) ?: return true
        val attackCost = getAttackCost(targetData, target)
        if (data.power < attackCost) {
            // TODO: ADD MESSAGE
            return true
        }
        val targetPlayer = instance.getPlayerByUuid(targetData.uuid.toUUID())
        val taken = when(buildingBlock) {
            Block.AIR -> { true }
            Block.LILY_PAD -> {
                ClaimWaterItem.destroyPlayerRaft(buildingPoint)
                targetData.blocks--
                instance.setBlock(target.playerPosition, Block.SAND)
                instance.setBlock(buildingPoint, Block.AIR)
                false
            }
            Barrack.block -> run {
                targetData.barracks.count--
                if (waterBlock.defaultState() != Block.WATER) {
                    data.barracks.count++
                    return@run true
                }
                attackRaft(targetData, target)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                false
            }
            MatterContainer.block -> run {
                targetData.matterContainers.count--
                if (waterBlock.defaultState() != Block.WATER) {
                    data.matterContainers.count++
                    return@run true
                }
                attackRaft(targetData, target)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                false
            }
            MatterExtractor.block -> run {
                targetData.matterExtractors.count--
                if (waterBlock.defaultState() != Block.WATER) {
                    data.matterExtractors.count++
                    return@run true
                }
                attackRaft(targetData, target)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                false
            }
            TrainingCamp.block -> run {
                targetData.trainingCamps.count--
                if (waterBlock.defaultState() != Block.WATER) {
                    data.trainingCamps.count++
                    return@run true
                }
                attackRaft(targetData, target)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                false
            }
            else -> run {
                // TODO: ADD PARTICLES
                val wallLevel = buildingBlock.wallLevel
                if (wallLevel == 0) return@run true
                if (wallLevel == 1) {
                    instance.setBlock(buildingPoint, if (waterBlock.defaultState() == Block.WATER) Block.LILY_PAD else Block.AIR)
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
        data.attackCooldown = getAttackCooldown(targetData, buildingBlock.wallLevel)
        event.player.sendPacket(
            SetCooldownPacket(
                getItem(event.player.uuid).material().id(),
                data.attackCooldown.ticks
            )
        )
        data.power -= attackCost
        data.updateBossBars()
        targetData.updateBossBars()

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}