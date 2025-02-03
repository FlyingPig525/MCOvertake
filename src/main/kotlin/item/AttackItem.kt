package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.data.player.PlayerData
import io.github.flyingpig525.data.player.PlayerData.Companion.getDataByPoint
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.ksp.Item
import io.github.flyingpig525.wall.getWallAttackCost
import io.github.flyingpig525.wall.lastWall
import io.github.flyingpig525.wall.wallLevel
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
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

@Item
object AttackItem : Actionable {

    override val identifier: String = "block:attack"
    override val itemMaterial: Material = Material.DIAMOND_SWORD

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return gameItem(itemMaterial, identifier) {
            val player = instance.instance.getPlayerByUuid(uuid) ?: return ERROR_ITEM
            val target = player.getTrueTarget(20) ?: return ERROR_ITEM
            val buildingBlock = instance.instance.getBlock(target.buildingPosition)
            val data = player.data ?: return ERROR_ITEM
            val targetData = getAttacking(player) ?: return ERROR_ITEM
            val targetName = targetData.playerDisplayName
            val preAttackData = ActionData.AttackCostCalculation(data, instance.instance, player).apply {
                wallLevel = buildingBlock.wallLevel
                this.targetData = targetData
            }.let { data.research.onPreAttack(it) }
            val attackCost = getAttackCost(
                preAttackData.targetData,
                target.buildingPosition,
                instance.instance,
                preAttackData.wallLevel,
                data.research.basicResearch.adjacentWallPercentageDecrease
            )
            val postAttack = ActionData.Attack(data, instance.instance, player).apply {
                attackCooldown = getAttackCooldown(preAttackData.targetData, preAttackData.wallLevel)
                this.attackCost = attackCost
                this.targetData = preAttackData.targetData
            }.also { data.research.onPostAttack(it) }

            itemName = "<red>$ATTACK_SYMBOL <bold>Attack $targetName</bold> <gray>- <red>$POWER_SYMBOL <bold>${postAttack.attackCost}".asMini().asComponent()
        }
    }

    private fun getAttacking(player: Player): PlayerData? {
        val players = instances.fromInstance(player.instance)!!.blockData
        val target = player.getTrueTarget(20)!!
        return players.getDataByPoint(target.playerPosition, player.instance)
    }

    private fun getAttackCost(targetData: PlayerData, wall: Point, instance: Instance, wallLevel: Int, percentageDecrease: Double): Int {
        var additiveModifier = 0
        additiveModifier += getWallAttackCost(
            wall,
            instance,
            targetData.block,
            customWallLevel = wallLevel,
            basePercentage = 1.05 - percentageDecrease + targetData.research.basicResearch.adjacentWallPercentageIncrease
        )
        return (targetData.baseAttackCost) + additiveModifier
    }

    private fun getAttackCooldown(targetData: PlayerData, wallLevel: Int): Cooldown {
        var cooldownTicks = 20L
        if (wallLevel <= 10) {
            cooldownTicks += 3 * wallLevel
        } else {
            cooldownTicks += (wallLevel * wallLevel) / 3
        }
        return Cooldown(Duration.ofMillis(cooldownTicks*50))
    }

    private fun attackRaft(targetData: PlayerData, point: Point, instance: Instance) {
        ClaimWaterItem.destroyPlayerRaft(point.withY(40.0), instance)
        targetData.blocks--
        instance.setBlock(point.withY(38.0), Block.SAND)
        instance.setBlock(point.buildingPosition, Block.AIR)
        // TODO: PARTICLES
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data_ = event.player.data ?: return true
        if (!data_.attackCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        val buildingPoint = target.buildingPosition
        val playerBlock = instance.getBlock(target.playerPosition)
        val waterBlock = instance.getBlock(target.visiblePosition)
        val buildingBlock = instance.getBlock(buildingPoint)
        if (playerBlock == Block.GRASS_BLOCK || playerBlock == Block.SAND || playerBlock == data_.block) {
            return true
        }
        val _targetData = getAttacking(event.player) ?: return true
        val preAttackData = ActionData.AttackCostCalculation(data_, instance, event.player).apply {
            wallLevel = buildingBlock.wallLevel
            targetData = _targetData
        }.let { data_.research.onPreAttack(it) }
        val attackCost = getAttackCost(
            preAttackData.targetData,
            target.buildingPosition,
            instance,
            preAttackData.wallLevel,
            data_.research.basicResearch.adjacentWallPercentageDecrease
        )
        val postAttack = ActionData.Attack(data_, instance, event.player).apply {
            attackCooldown = getAttackCooldown(preAttackData.targetData, preAttackData.wallLevel)
            this.attackCost = attackCost
            this.targetData = preAttackData.targetData
        }.also { data_.research.onPostAttack(it) }
        val data = postAttack.playerData
        if (data.power < postAttack.attackCost) {
            event.player.sendMessage("<red><bold>Not enough Power </bold>(${data.power}/${postAttack.attackCost})".asMini())
            return true
        }
        val targetPlayer = instance.getPlayerByUuid(postAttack.targetData.uuid.toUUID())
        val targetData = postAttack.targetData
        // TODO: REFACTOR THIS TO USE BUILDING REFERENCES
        val taken = when(buildingBlock) {
            Block.AIR -> { true }
            Block.LILY_PAD -> {
                ClaimWaterItem.destroyPlayerRaft(buildingPoint, instance)
                targetData.blocks--
                instance.setBlock(target.playerPosition, Block.SAND)
                instance.setBlock(buildingPoint, Block.AIR)
                false
            }
            else -> run {
                if (Building.blockIsBuilding(buildingBlock)) {
                    val building = Building.getBuildingByBlock(buildingBlock)!!
                    val buildingRef = building.playerRef.get(data.buildings)
                    val targetRef = building.playerRef.get(targetData.buildings)
                    targetRef.count--
                    if (waterBlock.defaultState() != Block.WATER) {
                        buildingRef.count++
                        return@run true
                    }
                    attackRaft(targetData, target, instance)
                    if (targetPlayer != null) {
                        SelectBuildingItem.updatePlayerItem(targetPlayer)
                    }
                    return@run false
                }
                // TODO: ADD PARTICLES
                val wallLevel = buildingBlock.wallLevel
                if (wallLevel == 0) return@run true
                if (wallLevel == 1) {
                    instance.setBlock(buildingPoint, if (waterBlock.defaultState() == Block.WATER) Block.LILY_PAD else Block.AIR)
                    // PARTICLES
                } else {
                    instance.setBlock(buildingPoint, lastWall(wallLevel))
                    UpgradeWallItem.updateWall(buildingPoint, instance)
                    // PARTICLES
                }
                buildingPoint.repeatAdjacent { UpgradeWallItem.updateWall(it, instance) }
                false
            }
        }
        if (taken) {
            postAttack.playerData.blocks++
            postAttack.targetData.blocks--
            if (Building.blockIsBuilding(buildingBlock)) {
                claimWithParticle(event.player, target, postAttack.playerData.block, instance)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                SelectBuildingItem.updatePlayerItem(event.player)
            } else {
                claimWithParticle(event.player, target, postAttack.playerData.block, instance)
            }
        }
        data.attackCooldown = postAttack.attackCooldown
        data.sendPacket(
            SetCooldownPacket(
                itemMaterial.cooldownIdentifier,
                data.attackCooldown.ticks
            )
        )
        data.power -= postAttack.attackCost
        ActionData.Attacked(postAttack.targetData, instance, targetPlayer).apply {
            this.attackerData = data
            this.attackerPlayer = event.player
        }.also { targetData.research.onAttacked(it) }
        data.updateBossBars()
        targetData.updateBossBars()

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid, instances.fromInstance(player.instance)!!)
    }
}