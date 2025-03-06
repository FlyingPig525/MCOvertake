package io.github.flyingpig525.command

import io.github.flyingpig525.data.player.PlayerData.Companion.playerData
import net.bladehunt.kotstom.dsl.kommand.defaultExecutor
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.entity.Player
import kotlin.time.Duration.Companion.seconds

val playtimeCommand = kommand("playtime") {

    defaultExecutor { player, ctx ->
        val playerData = (player as Player).playerData
        if (playerData == null) {
            player.sendMessage("<red>You must be in an instance to run this command".asMini())
            return@defaultExecutor
        }
        val duration = playerData.playtime.seconds
        player.sendMessage("<gray>You have played this instance for: <gold>${duration}".asMini())
    }
}