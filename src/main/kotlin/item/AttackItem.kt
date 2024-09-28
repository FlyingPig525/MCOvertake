package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.*
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.data.PlayerData.Companion.getDataByBlock
import io.github.flyingpig525.data.PlayerData.Companion.getDataByPoint
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
import java.util.*

object AttackItem : Actionable {

    init {
        Actionable.registry += this
    }

    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.DIAMOND_SWORD) {
            val player = instance.getPlayerByUuid(uuid)!!
            val target = player.getTargetBlockPosition(20) ?: return item {}
            val targetUUID = getAttacking(player)?.uuid ?: ""
            val targetData = players[targetUUID]
            val targetName = targetData?.playerDisplayName ?: ""
            val attackCost = getAttackCost(targetData, target)

            itemName = "<red>$ATTACK_SYMBOL <bold>Attack $targetName</bold> <gray>- <red>$POWER_SYMBOL <bold>$attackCost".asMini().asComponent()
        }
    }

    private fun getAttacking(player: Player): PlayerData? {
        val target = player.getTargetBlockPosition(20)!!
        // TODO: AFTER ADDING WALLS ADD WALL THINGS HERE
        return players.getDataByPoint(target)
    }

    private fun getAttackCost(player: Player): Int {
        val target = player.getTargetBlockPosition(20)!!
        // TODO: AFTER ADDING WALLS ADD WALL THINGS HERE
        val targetData = getAttacking(player)
        val targetUUID = targetData?.uuid ?: ""
        val targetAttackCost = (targetData?.baseAttackCost ?: 15)
        return targetAttackCost
    }

    private fun getAttackCost(targetData: PlayerData?, target: Point): Int {
        // TODO: AFTER ADDING WALLS ADD WALL THINGS HERE
        return (targetData?.baseAttackCost ?: 15)
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val target = event.player.getTargetBlockPosition(20) ?: return true
        val block = instance.getBlock(target)
        val data = players[event.player.uuid.toString()]!!
        if (block == Block.GRASS_BLOCK || block == data.block) {
            return true
        }
        val attackCost = getAttackCost(event.player)
        if (data.power < attackCost) {
            // TODO: ADD MESSAGE
            return true
        }
        val targetData = getAttacking(event.player) ?: return true
        val targetPlayer = instance.getPlayerByUuid(targetData.uuid.toUUID())
        val taken = when(block) {
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
                // TODO: ADD WALL THINGS
                true
            }
        }
        if (taken) {
            data.blocks++
            targetData.blocks--
            if (Building.blockIsBuilding(block)) {
                claimWithParticle(event.player, target.sub(0.0, 1.0, 0.0), data.block)
                if (targetPlayer != null) {
                    SelectBuildingItem.updatePlayerItem(targetPlayer)
                }
                SelectBuildingItem.updatePlayerItem(event.player)
            } else {
                claimWithParticle(event.player, target, data.block)
            }
        }
        data.updateBossBars()
        targetData.updateBossBars()

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid)
    }
}