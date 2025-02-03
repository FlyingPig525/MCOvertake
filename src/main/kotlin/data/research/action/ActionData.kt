package io.github.flyingpig525.data.research.action

import io.github.flyingpig525.building.Building
import io.github.flyingpig525.data.player.BlockData
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.utils.time.Cooldown
import java.time.Duration

sealed class ActionData(val playerData: BlockData, val instance: Instance?, val player: Player?) {
    class Attacked(playerData: BlockData, instance: Instance, player: Player?) : ActionData(
        playerData, instance,
        player
    ) {
        var attackerData: BlockData = BlockData.NONE
        var attackerPlayer: Player? = null
    }

    class BuildWall(playerData: BlockData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost: Int = 0
        var cooldown: Cooldown = Cooldown(Duration.ZERO)
    }

    class ClaimLand(playerData: BlockData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var claimCost = playerData.claimCost
        var claimCooldown: Cooldown = Cooldown(Duration.ofMillis(playerData.maxClaimCooldown))
    }

    class PlaceColony(playerData: BlockData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost = 0
        var cooldown = Cooldown(Duration.ZERO)
    }

    class PlaceRaft(playerData: BlockData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost: Int = 0
        var cooldown: Cooldown = Cooldown(Duration.ZERO)
    }

    class Attack(playerData: BlockData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var attackCost: Int = 0
        var attackCooldown: Cooldown = Cooldown(Duration.ZERO)
        var targetData: BlockData = BlockData.NONE
    }

    class AttackCostCalculation(playerData: BlockData, instance: Instance, player: Player) : ActionData(
        playerData, instance, player
    ) {
        var wallLevel = 0
        var targetData: BlockData = BlockData.NONE
    }

    class UpgradeWall(playerData: BlockData, instance: Instance) : ActionData(
        playerData, instance,
        null
    ) {
        var cost: Int = 0
        var cooldown: Cooldown = Cooldown(Duration.ZERO)
    }

    class DestroyBuilding(playerData: BlockData, instance: Instance, player: Player) : ActionData(playerData, instance,
        player
    ) {
        var wallLevel: Int = 0
        var building: Building? = null
    }

    class MatterBuildingTick(playerData: BlockData) : ActionData(playerData, null, null) {
        var increase: Double = 0.0
    }
}