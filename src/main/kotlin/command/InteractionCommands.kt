package io.github.flyingpig525.command

import io.github.flyingpig525.data
import net.bladehunt.kotstom.command.Kommand
import net.bladehunt.kotstom.dsl.kommand.buildSyntax
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.minecraft.ArgumentIntRange
import net.minestom.server.command.builder.arguments.number.ArgumentFloat
import net.minestom.server.command.builder.arguments.number.ArgumentInteger

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

val flightSpeedCommand = kommand {
    name = "fs"

    val speedArg = ArgumentFloat("speed")

    buildSyntax(speedArg) {
        executor {
            val speev = context.get(speedArg)
            player.flyingSpeed = (speev / 100f)
        }
    }
}