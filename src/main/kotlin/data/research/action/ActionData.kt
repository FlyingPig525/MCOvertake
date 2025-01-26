package io.github.flyingpig525.data.research.action

import io.github.flyingpig525.building.Building
import io.github.flyingpig525.data.player.PlayerData
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import kotlin.reflect.KProperty0

sealed class ActionData(val playerData: PlayerData, val instance: Instance?, val player: Player?) {
    class Attacked(playerData: PlayerData, instance: Instance, player: Player?) : ActionData(
        playerData, instance,
        player
    ) {
        var attackerData: PlayerData = PlayerData.NONE
        var attackerPlayer: Player? = null
    }

    class BuildWall(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost: Int = 0
        var cooldown: Cooldown = Cooldown(Duration.ZERO)
    }

    class ClaimLand(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var claimCost = playerData.claimCost
        var claimCooldown: Cooldown = Cooldown(Duration.ofMillis(playerData.maxClaimCooldown))
    }

    class PlaceColony(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost = 0
        var cooldown = Cooldown(Duration.ZERO)
    }

    class PlaceRaft(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost: Int = 0
        var cooldown: Cooldown = Cooldown(Duration.ZERO)
    }

    class Attack(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var attackCost: Int = 0
        var attackCooldown: Cooldown = Cooldown(Duration.ZERO)
        var targetData: PlayerData = PlayerData.NONE
    }

    class AttackCostCalculation(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance, player
    ) {
        var wallLevel = 0
        var targetData: PlayerData = PlayerData.NONE
    }

    class UpgradeWall(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var cost: Int = 0
        var cooldown: Cooldown = Cooldown(Duration.ZERO)
    }

    class DestroyBuilding(playerData: PlayerData, instance: Instance, player: Player) : ActionData(playerData, instance,
        player
    ) {
        var wallLevel: Int = 0
        var building: Building? = null
    }

    class MatterBuildingTick(playerData: PlayerData) : ActionData(playerData, null, null) {
        var increase: Double = 0.0
    }
}