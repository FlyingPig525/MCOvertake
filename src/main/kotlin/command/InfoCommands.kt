package io.github.flyingpig525.command

import io.github.flyingpig525.data.player.PlayerData.Companion.playerData
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini
import kotlin.time.Duration.Companion.seconds

val playtimeCommand = kommand {
    name = "playtime"

    defaultExecutor {
        val playerData = player.playerData
        if (playerData == null) {
            player.sendMessage("<red>You must be in an instance to run this command".asMini())
            return@defaultExecutor
        }
        val duration = playerData.playtime.seconds
        player.sendMessage("<gray>You have played this instance for: <gold>${duration}".asMini())
    }
}