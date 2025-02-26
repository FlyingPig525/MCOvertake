package io.github.flyingpig525.command

import io.github.flyingpig525.data
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini

val tpAlertCommand = kommand {
    name = "tpAlert"

    defaultExecutor {
        val data = player.data ?: return@defaultExecutor
        if (data.alertLocation == null) {
            player.sendMessage("<red><bold>No alert found".asMini())
            return@defaultExecutor
        }
        player.sendMessage("<green>Sending you to the alert location".asMini())
        player.teleport(data.alertLocation!!)
        player.isFlying = true
    }
}