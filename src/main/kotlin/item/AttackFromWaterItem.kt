package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.item.AttackItem.attack
import io.github.flyingpig525.item.AttackItem.getAttackCooldown
import io.github.flyingpig525.item.AttackItem.getAttackCost
import io.github.flyingpig525.item.AttackItem.getAttacking
import io.github.flyingpig525.ksp.Item
import io.github.flyingpig525.wall.wallLevel
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.time.Instant
import java.util.*

@Item
object AttackFromWaterItem : Actionable {
    override val identifier: String = "block:water_attack"
    override val itemMaterial: Material = Material.DIAMOND_SWORD

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return gameItem(itemMaterial, identifier) {
            val player = instance.instance.getPlayerByUuid(uuid) ?: return ERROR_ITEM
            val target = player.getTrueTarget(20) ?: return ERROR_ITEM
            val buildingBlock = instance.instance.getBlock(target.buildingPosition)
            val data = player.data ?: return ERROR_ITEM
            val targetData = getAttacking(player) ?: return ERROR_ITEM
            val targetName = targetData.playerDisplayName
            val preAttackData = ActionData.WaterAttackCostCalculation(data, instance.instance, player).apply {
                wallLevel = buildingBlock.wallLevel
                this.targetData = targetData
                costAddition = 50
            }.let { data.research.onWaterAttackCostCalculation(it) }
            val attackCost = getAttackCost(
                preAttackData.targetData,
                target.buildingPosition,
                instance.instance,
                preAttackData.wallLevel,
                data.research.basicResearch.adjacentWallPercentageDecrease,
                baseAttackCost = 0
            ) + preAttackData.costAddition
            val postAttack = ActionData.Attack(data, instance.instance, player).apply {
                attackCooldown = getAttackCooldown(preAttackData.targetData, preAttackData.wallLevel)
                this.attackCost = attackCost
                this.targetData = preAttackData.targetData
            }.also { data.research.onPostAttack(it) }

            itemName = "<red>$ATTACK_SYMBOL <bold>Attack $targetName From Water</bold> <gray>- <red>$POWER_SYMBOL <bold>${postAttack.attackCost}".asMini().asComponent()
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data_ = event.player.data ?: return true
        if (!data_.attackCooldown.isReady(Instant.now().toEpochMilli())) return true
        val target = event.player.getTrueTarget(20) ?: return true
        val buildingPoint = target.buildingPosition
        val playerBlock = instance.getBlock(target.playerPosition)
        val buildingBlock = instance.getBlock(buildingPoint)
        if (playerBlock == Block.GRASS_BLOCK || playerBlock == Block.SAND || playerBlock == data_.block) {
            return true
        }
        val _targetData = getAttacking(event.player) ?: return true
        val preAttackData = ActionData.WaterAttackCostCalculation(data_, instance, event.player).apply {
            wallLevel = buildingBlock.wallLevel
            targetData = _targetData
            costAddition = 50
        }.let { data_.research.onWaterAttackCostCalculation(it) }
        val attackCost = getAttackCost(
            preAttackData.targetData,
            target.buildingPosition,
            instance,
            preAttackData.wallLevel,
            data_.research.basicResearch.adjacentWallPercentageDecrease,
            baseAttackCost = 0
        ) + preAttackData.costAddition
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
        attack(event, postAttack)

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[0] = getItem(player.uuid, player.gameInstance ?: return)

    }
}