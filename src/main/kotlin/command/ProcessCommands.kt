package io.github.flyingpig525.command

import io.github.flyingpig525.data.player.permission.Permission
import io.github.flyingpig525.permissionManager
import io.github.flyingpig525.tick
import io.github.flyingpig525.tpsMonitor
import net.bladehunt.kotstom.BenchmarkManager
import net.bladehunt.kotstom.dsl.kommand.buildSyntax
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.text.Component

val tickCommand = kommand {
    name = "tick"
    defaultExecutor {
        val tps = tpsMonitor.getTps()
        val tps1 = tpsMonitor.getAvgTps1Min()
        val tps5 = tpsMonitor.getAvgTps5Min()
        val tps15 = tpsMonitor.getAvgTps15Min()
        player.sendMessage("<dark_gray>-----<gold><bold>Tick Data</bold><dark_gray>-----".asMini().append(Component.newline())
            .append("<gold>Tick: <bold>$tick".asMini()).append(Component.newline())
            .append("<${if (tps <= 15) "red" else "gold"}>TPS: <bold>${tps}".asMini()).append(Component.newline())
            .append("<${if (tps1 <= 17) "red" else "gold"}>Average TPS 1 Min: <bold>${tps1}".asMini()).append(Component.newline())
            .append("<${if (tps5 <= 18) "red" else "gold"}>Average TPS 5 Min: <bold>${tps5}".asMini()).append(Component.newline())
            .append("<${if (tps15 <= 19) "red" else "gold"}>Average TPS 15 Min: <bold>${tps15}".asMini())
        )
    }
}

val gcCommand = kommand {
    name = "gc"

    buildSyntax {
        condition {
            permissionManager.hasPermission(player, Permission("process.garbage_collect"))
        }
        executor {
            var ramUsage = (BenchmarkManager.usedMemory / 1e6).toLong()
            player.sendMessage("Before: ${ramUsage}mb")
            System.gc()
            ramUsage = (BenchmarkManager.usedMemory / 1e6).toLong()
            player.sendMessage("After: ${ramUsage}mb")
        }
    }
}