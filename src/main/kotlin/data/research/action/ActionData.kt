package io.github.flyingpig525.data.research.action

import io.github.flyingpig525.data.PlayerData
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
// TODO: FINISH ADDING EVENTS
sealed class ActionData(val playerData: PlayerData, val instance: Instance, val player: Player) {
    class Attacked(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    )

    class BuildWall(playerData: PlayerData, instance: Instance, player: Player) :
        ActionData(playerData, instance, player)

    class ClaimLand(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var claimCost = playerData.claimCost
        var claimCooldown = playerData.maxClaimCooldown
    }

    class PlaceColony(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    )

    class PlaceRaft(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    )

    class PostAttack(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    ) {
        var attackCost: Int = 0
        var attackCooldown: Cooldown = Cooldown(Duration.ZERO)
        var targetData: PlayerData = PlayerData("", Block.AIR)
    }

    class PreAttack(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance, player
    ) {
        var wallLevel = 0
        var targetData: PlayerData = PlayerData.NONE
    }

    class UpgradeWall(playerData: PlayerData, instance: Instance, player: Player) : ActionData(
        playerData, instance,
        player
    )

    class DestroyBuilding(playerData: PlayerData, instance: Instance, player: Player) : ActionData(playerData, instance,
        player
    )
}