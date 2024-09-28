package io.github.flyingpig525.data

import io.github.flyingpig525.*
import io.github.flyingpig525.building.*
import io.github.flyingpig525.item.SelectBlockItem
import io.github.flyingpig525.item.SelectBuildingItem
import io.github.flyingpig525.serializers.BlockSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.toPlainText
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.util.*
import kotlin.collections.Map

@Serializable
class PlayerData(val uuid: String, @Serializable(BlockSerializer::class) val block: Block) {
    val playerDisplayName = instance.getPlayerByUuid(uuid.toUUID())?.name?.toPlainText()
    var blocks: Int = 0
    val trainingCamps = TrainingCamp()
    val trainingCampCost: Int get() = (trainingCamps.count * 25) + 25
    val barracks = Barrack()
    val maxPower: Int get() = 100 + barracks.count * 25
    val barracksCost: Int get() = (barracks.count * 20) + 20
    val matterExtractors = MatterExtractor()
    val extractorCost: Int get() = (matterExtractors.count * 25) + 25
    val matterContainers = MatterContainer()
    val containerCost: Int get() = (matterContainers.count * 20) + 20
    val maxMatter: Int get() = 100 + matterContainers.count * 25
    var claimLevel: Int = 0
    val claimCost: Int get() = blocks.floorDiv(500) + 5
    @Transient var claimCooldown = Cooldown(Duration.ofMillis(maxClaimCooldown))
    val maxClaimCooldown get() = (((blocks.toLong() / 1000.0) * 50.0) + 1000.0).toLong()
    val colonyCost: Int get() = claimCost * 10
    @Transient var colonyCooldown = Cooldown(Duration.ofSeconds(if (blocks > 0) 15 else 0))
    @Transient var attackCooldown = Cooldown(Duration.ofSeconds(1))
    var power: Double = 100.0
        set(value) {
            field = value.coerceIn(0.0..maxPower.toDouble())
            updateBossBars()
        }
    @Transient val powerBossBar: BossBar = BossBar.bossBar(
                "<red>$POWER_SYMBOL Power <gray>-<red> $power/$maxPower".asMini(),
                (power / maxPower).toFloat(),
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
            )
    var organicMatter: Double = 100.0
        set(value) {
            field = value.coerceIn(0.0..maxMatter.toDouble())
            updateBossBars()
        }
    @Transient val matterBossBar: BossBar = BossBar.bossBar(
                "<green>$MATTER_SYMBOL Organic Matter <gray>- <green>$organicMatter/$maxMatter".asMini(),
                (organicMatter / maxMatter).toFloat(),
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
            )
    var mechanicalParts: Int = 0
        set(value) {
            field = value
            updateBossBars()
        }
    val disposableResourcesUsed: Int get() {
        val barrack = barracks.resourceUse
        val matterContainer = matterContainers.resourceUse
        val matterExtractor = matterExtractors.resourceUse
        val trainingCamp = trainingCamps.resourceUse
        return barrack + matterExtractor + matterContainer + trainingCamp
    }
    val maxDisposableResources: Int get() = (blocks / 5) + 30
    @Transient val resourcesBossBar: BossBar = BossBar.bossBar(
        "<${
            if (disposableResourcesUsed > maxDisposableResources)
                "light_purple"
            else "white"
        }>$RESOURCE_SYMBOL Disposable Resources <gray>- <${
            if (disposableResourcesUsed > maxDisposableResources)
                "light_purple"
            else "white"
        }>$disposableResourcesUsed/$maxDisposableResources".asMini(),
        (disposableResourcesUsed.toFloat() / maxDisposableResources.toFloat()).coerceIn((0f..1f)),
        BossBar.Color.WHITE,
        BossBar.Overlay.PROGRESS
    )
    val baseAttackCost: Int get() {
        return 15
    }

    fun tick(instance: Instance) {
        organicMatter =
            ((matterExtractors.count * 0.5 + 0.5) + organicMatter)
        val player = instance.getPlayerByUuid(UUID.fromString(uuid))
        if (player != null) {
            updateBossBars()
        }
    }

    fun updateBossBars() {
        powerBossBar.name("<red>$POWER_SYMBOL Power <gray>-<red> $power/$maxPower".asMini())
        powerBossBar.progress((power / maxPower).toFloat())
        matterBossBar.name("<green>$MATTER_SYMBOL Organic Matter <gray>-<green> $organicMatter/$maxMatter".asMini())
        matterBossBar.progress((organicMatter / maxMatter).toFloat())
        val overflow = disposableResourcesUsed > maxDisposableResources
        resourcesBossBar.name("<${if (overflow) "light_purple" else "white"}>$RESOURCE_SYMBOL Disposable Resources <gray>- <${if (overflow) "light_purple" else "white"}>$disposableResourcesUsed/$maxDisposableResources".asMini())
        resourcesBossBar.progress((disposableResourcesUsed.toFloat() / maxDisposableResources.toFloat()).coerceIn(0f..1f))
        resourcesBossBar.color(if (disposableResourcesUsed > maxDisposableResources) BossBar.Color.PURPLE else BossBar.Color.WHITE)
    }

    fun showBossBars(player: Player) {
        with(player) {
            showBossBar(powerBossBar)
            showBossBar(matterBossBar)
            showBossBar(resourcesBossBar)
        }
    }



    fun setupPlayer(player: Player) {
        showBossBars(player)
        updateBossBars()
        SelectBuildingItem.setItemSlot(player)
        SelectBlockItem.setItemSlot(player)

        player.inventory.helmet = item(Material.fromNamespaceId(block.namespace())!!)
    }

    companion object {
        fun Map<String, PlayerData>.getDataByBlock(block: Block): PlayerData? {
            return values.find { it.block == block }
        }
        fun Map<String, PlayerData>.getDataByPoint(point: Point): PlayerData? {
            val block = instance.getBlock(point)
            val under = instance.getBlock(point.sub(0.0, 1.0, 0.0))
            return values.find { it.block == block || it.block == under }
        }
        fun Map<String, PlayerData>.toBlockSortedList(): List<PlayerData> {
            return values.sortedByDescending { it.blocks }
        }
    }
}