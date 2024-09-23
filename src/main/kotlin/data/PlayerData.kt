package io.github.flyingpig525.data

import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.instance
import io.github.flyingpig525.serializers.BlockSerializer
import io.github.flyingpig525.serializers.CooldownSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.util.*

@Serializable
class PlayerData(val uuid: String, @Serializable(BlockSerializer::class) val block: Block) {
    var blocks: Int = 0
    val trainingCamps = Building.TrainingCamp()
    val trainingCampCost: Int get() = (trainingCamps.count * 25) + 25
    val barracks = Building.Barrack()
    val maxPower: Int get() = 100 + barracks.count * 25
    val barracksCost: Int get() = (barracks.count * 10) + 20
    val matterExtractors = Building.MatterExtractor()
    val extractorCost: Int get() = (matterExtractors.count * 25) + 25
    val matterContainers = Building.MatterContainer()
    val containerCost: Int get() = (matterContainers.count * 20) + 20
    val maxMatter: Int get() = 100 + matterContainers.count * 25
    var claimLevel: Int = 0
    val claimCost: Int get() = blocks.floorDiv(500) + 5
    @Transient var claimCooldown = Cooldown(Duration.ofSeconds(maxClaimCooldownSeconds))
    val maxClaimCooldownSeconds get() = (((blocks / 100) * 50) + 500).toLong()
    val colonyCost: Int get() = claimCost * 10
    @Transient var colonyCooldown = Cooldown(Duration.ofSeconds(10))
    @Transient var attackCooldown = Cooldown(Duration.ofSeconds(1))
    var power: Double = 100.0
        set(value) {
            field = value
            updateBossBars()
        }
    @Transient val powerBossBar: BossBar = BossBar.bossBar(
                "<red>$POWER_SYMBOL Power <gray>-<red> $power/$maxPower".asMini(),
                (power / maxPower).toFloat().coerceIn(0.0f..1.0f),
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
            )
    var organicMatter: Double = 100.0
        set(value) {
            field = value
            updateBossBars()
        }
    @Transient val matterBossBar: BossBar = BossBar.bossBar(
                "<green>$MATTER_SYMBOL Organic Matter <gray>-<green> $organicMatter/$maxMatter".asMini(),
                (organicMatter / maxMatter).toFloat().coerceIn(0.0f..1.0f),
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
            )
    var mechanicalParts: Int = 0
        set(value) {
            field = value
            updateBossBars()
        }

    fun tick(instance: Instance) {
        power =
            ((trainingCamps.count * 0.5 + 0.5) + power).coerceIn(0.0..(maxPower).toDouble())
        organicMatter =
            ((matterExtractors.count * 0.5 + 0.5) + organicMatter).coerceIn(0.0..(maxMatter).toDouble())
        val player = instance.getPlayerByUuid(UUID.fromString(uuid))
        if (player != null) {
            updateBossBars()
        }
    }

    fun updateBossBars() {
        val player = instance.getPlayerByUuid(UUID.fromString(uuid)) ?: return
        powerBossBar.name("<red>$POWER_SYMBOL Power <gray>-<red> $power/$maxPower".asMini())
        powerBossBar.progress((power / maxPower).toFloat().coerceIn(0.0f..1.0f))
        player.showBossBar(powerBossBar)
        matterBossBar.name("<green>$MATTER_SYMBOL Organic Matter <gray>-<green> $organicMatter/$maxMatter".asMini())
        matterBossBar.progress((organicMatter / maxMatter).toFloat().coerceIn(0.0f..1.0f))
        player.showBossBar(matterBossBar)

    }
}