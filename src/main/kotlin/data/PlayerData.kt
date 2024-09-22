package io.github.flyingpig525.data

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import java.util.*

class PlayerData(val uuid: UUID, val block: Block) {
    var blocks: Int = 0
    val trainingCamps = Building.TrainingCamp()
    val trainingCampCost get() = (trainingCamps.count * 25) + 25
    val barracks = Building.Barrack()
    val maxPower get() = 100 + barracks.count * 25
    val barracksCost get() = (barracks.count * 10) + 20
    val matterExtractors = Building.MatterExtractor()
    val extractorCost get() = (matterExtractors.count * 25) + 25
    val matterContainers = Building.MatterContainer()
    val containerCost get() = (matterContainers.count * 20) + 20
    val maxMatter get() = 100 + matterContainers.count * 25
    var claimLevel: Int = 0
    val claimCost: Int get() = blocks.floorDiv(500) + 5
    val colonyCost: Int get() = claimCost * 10
    var power: Double = 100.0
    val powerBossBar: BossBar
        get() {
            val bar = BossBar.bossBar(
                "<red>$POWER_SYMBOL Power <gray>-<red> $power/$maxPower".asMini(),
                (power / maxPower).toFloat().coerceIn(0.0f..1.0f),
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
            )
            lastPowerBossBar = bar
            return bar
        }
    private var lastPowerBossBar = powerBossBar
    var organicMatter: Double = 100.0
    val matterBossBar: BossBar
        get() {
            val bar = BossBar.bossBar(
                "<green>$MATTER_SYMBOL Organic Matter <gray>-<green> $organicMatter/$maxMatter".asMini(),
                (organicMatter / maxMatter).toFloat().coerceIn(0.0f..1.0f),
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
            )
            lastMatterBossBar = bar
            return bar
        }
    private var lastMatterBossBar = matterBossBar
    var mechanicalParts: Int = 0

    fun tick(instance: Instance) {
        power =
            ((trainingCamps.count * 0.5 + 0.5) + power).coerceIn(0.0..(maxPower).toDouble())
        organicMatter =
            ((matterExtractors.count * 0.5 + 0.5) + organicMatter).coerceIn(0.0..(maxMatter).toDouble())
        val player = instance.getPlayerByUuid(uuid)
        if (player != null) {
            updateBossBars(player)
        }
    }

    fun updateBossBars(player: Player) {
        player.hideBossBar(lastPowerBossBar)
        player.showBossBar(powerBossBar)
        player.hideBossBar(lastMatterBossBar)
        player.showBossBar(matterBossBar)

    }
}