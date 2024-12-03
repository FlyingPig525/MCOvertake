package io.github.flyingpig525

import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.listen
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.monitoring.TickMonitor
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.MathUtils
import net.minestom.server.utils.time.TimeUnit
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicReference

// More stolen stuff
class TpsMonitor {
    private val TICK_RATE = 20
    private var lastTime: Long = 0
    private var tps: Double = 0.0

    private val tps1Min = LinkedList<Double>()
    private val tps5Min = LinkedList<Double>()
    private val tps15Min = LinkedList<Double>()

    fun start() {
        val scheduler = SchedulerManager
        lastTime = System.nanoTime()
        scheduler.submitTask {
            val currentTime = System.nanoTime()
            val elapsedSeconds = (currentTime - lastTime) / 1_000_000_000.0
            tps = Math.min(TICK_RATE.toDouble(), TICK_RATE.toDouble() / elapsedSeconds)
            lastTime = currentTime

            updateTpsHistory(tps)

            TaskSchedule.tick(1)
        }
        val lastTick = AtomicReference<TickMonitor>()
        GlobalEventHandler.listen<ServerTickMonitorEvent> { event -> lastTick.set(event.tickMonitor) }
        val benchmarkManager = MinecraftServer.getBenchmarkManager()
        benchmarkManager.enable(Duration.ofSeconds(5))
        scheduler.buildTask {
            if (lastTick.get() == null || MinecraftServer.getConnectionManager().onlinePlayerCount == 0) return@buildTask
            var ramUsage = benchmarkManager.usedMemory
            ramUsage = (ramUsage / 1e6).toLong() // bytes to MB

            val tickMonitor: TickMonitor = lastTick.get()
            val header: Component = Component.text("RAM USAGE: $ramUsage MB")
                .append(Component.newline())
                .append(Component.text("TICK: $tick"))
                .append(Component.newline())
                .append(
                    Component.text(
                        "TICK TIME: " + MathUtils.round(
                            tickMonitor.tickTime,
                            2
                        ) + "ms"
                    )
                )
                .append(Component.newline())
                .append(
                    Component.text(
                        "ACQ TIME: " + MathUtils.round(
                            tickMonitor.acquisitionTime,
                            2
                        ) + "ms"
                    )
                )
            val footer: Component = benchmarkManager.cpuMonitoringMessage.append(Component.newline()).append(Component.text("TPS: ${getTps()}"))
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer)
        }.repeat(1, TimeUnit.SERVER_TICK).schedule()
    }

    private fun updateTpsHistory(currentTps: Double) {
        tps1Min.add(currentTps)
        if (tps1Min.size > 60) tps1Min.poll()

        tps5Min.add(currentTps)
        if (tps5Min.size > 300) tps5Min.poll()

        tps15Min.add(currentTps)
        if (tps15Min.size > 900) tps15Min.poll()
    }

    fun getTps(): Double {
        return tps
    }

    fun getAvgTps(tpsList: LinkedList<Double>): Double {
        return tpsList.stream().mapToDouble { it }.average().orElse(0.0)
    }

    fun getAvgTps1Min(): Double {
        return getAvgTps(tps1Min)
    }

    fun getAvgTps5Min(): Double {
        return getAvgTps(tps5Min)
    }

    fun getAvgTps15Min(): Double {
        return getAvgTps(tps15Min)
    }
}