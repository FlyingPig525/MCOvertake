package io.github.flyingpig525.data.player

import io.github.flyingpig525.*
import io.github.flyingpig525.building.*
import io.github.flyingpig525.data.player.config.PlayerConfig
import io.github.flyingpig525.data.research.ResearchContainer
import io.github.flyingpig525.item.*
import io.github.flyingpig525.serialization.BlockSerializer
import io.github.flyingpig525.wall.wallLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerTickEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.tag.Tag
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.reflect.KProperty0

@Serializable
class PlayerData(val uuid: String, @Serializable(BlockSerializer::class) val block: Block, var playerDisplayName: String) {
    @Transient var gameInstance: GameInstance? = null
    val playerConfig = PlayerConfig()
    var blocks: Int = 0
    val trainingCamps = TrainingCamp()
    val trainingCampCost: Int get() = genericBuildingCost(trainingCamps.count, 25)
    val barracks = Barrack()
    val maxPower: Int get() = 100 + barracks.count * 25
    val barracksCost: Int get() = (barracks.count * 20) + 20
    val matterExtractors = MatterExtractor()
    val extractorCost: Int get() = genericBuildingCost(matterExtractors.count, 25)
    val matterContainers = MatterContainer()
    val containerCost: Int get() = (matterContainers.count * 20) + 20
    val matterCompressors = MatterCompressionPlant()
    val matterCompressorCost: Int get() = (matterCompressors.count * 50) + 50
    val maxMatter: Int get() = 100 + matterContainers.count * 25
    val claimCost: Int get() = blocks.floorDiv(500) + 5
    val maxClaimCooldown get() = (((blocks.toLong() / 1000.0) * 50.0) + 1000.0).toLong()
    val colonyCost: Int get() = claimCost * 10
    val raftCost: Int get() = (blocks.floorDiv(10000) * 500) + 500
    val undergroundTeleporters = UndergroundTeleporter()
    val teleporterCost: Int get() = undergroundTeleporters.count * 1000 + 1000
    @Transient var claimCooldown = Cooldown(Duration.ofMillis(maxClaimCooldown))
    @Transient var colonyCooldown = Cooldown(Duration.ofSeconds(if (blocks > 0) 15 else 0))
    @Transient var attackCooldown = Cooldown(Duration.ofSeconds(10))
    @Transient var wallCooldown = Cooldown(Duration.ofSeconds(2))
    @Transient var wallUpgradeCooldown = Cooldown(Duration.ofSeconds(2))
    @Transient var raftCooldown = Cooldown(Duration.ofSeconds(20))
    var power: Double = 100.0
        set(value) {
            field = value.coerceIn(0.0..maxPower.toDouble())
            val player = gameInstance?.instance?.getPlayerByUuid(UUID.fromString(uuid))
            updateBossBars(player)
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
            val player = gameInstance?.instance?.getPlayerByUuid(UUID.fromString(uuid))
            updateBossBars(player)
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
            val player = gameInstance?.instance?.getPlayerByUuid(UUID.fromString(uuid))
            updateBossBars(player)
        }
    val disposableResourcesUsed: Int get() {
        val barrack = barracks.resourceUse
        val matterContainer = matterContainers.resourceUse
        val matterExtractor = matterExtractors.resourceUse
        val trainingCamp = trainingCamps.resourceUse
        val matterCompressor = matterCompressors.resourceUse
        val teleporter = undergroundTeleporters.resourceUse
        return barrack + matterExtractor + matterContainer + trainingCamp + matterCompressor + teleporter
    }
    val maxDisposableResources: Int get() = (blocks / 5) + 30
    @Transient val resourcesBossBar: BossBar = BossBar.bossBar(
        "<${
            if (disposableResourcesUsed > maxDisposableResources)
                "light_purple"
            else "aqua"
        }>$RESOURCE_SYMBOL Disposable Resources <gray>- <${
            if (disposableResourcesUsed > maxDisposableResources)
                "light_purple"
            else "aqua"
        }>$disposableResourcesUsed/$maxDisposableResources".asMini(),
        (disposableResourcesUsed.toFloat() / maxDisposableResources.toFloat()).coerceIn((0f..1f)),
        BossBar.Color.BLUE,
        BossBar.Overlay.PROGRESS
    )
    @Transient val mechanicalBossBar = BossBar.bossBar(
        "<white>$MECHANICAL_SYMBOL Mechanical Parts<gray> - <white>$mechanicalParts".asMini(),
        1f,
        BossBar.Color.WHITE,
        BossBar.Overlay.PROGRESS
    )
    val baseAttackCost: Int get() {
        return 15
    }
    @Transient val lastTeleporterPos: MutableSet<Point> = mutableSetOf()
    @Transient var targetWallLevel: Int = 0
    @Transient val wallUpgradeQueue: MutableList<Pair<Point, Int>> = mutableListOf()
    @Transient var handAnimationWasDrop = false
    @Transient var bulkWallQueueFirstPos: Point? = null
    @Transient var bulkWallQueueFirstPosJustReset = false

    fun tick(e: PlayerTickEvent) {
        if (wallUpgradeQueue.isNotEmpty() && wallUpgradeCooldown.isReady(Instant.now().toEpochMilli())) run {
            val (wallPos, targetLevel) = wallUpgradeQueue.first()
            val wall = e.instance.getBlock(wallPos)
            val currentLevel = wall.wallLevel
            if (currentLevel == 0 || currentLevel >= targetLevel) {
                wallUpgradeQueue.removeFirst()
                e.instance.getNearbyEntities(wallPos, 0.2).onEach { it.remove() }
                return@run
            }
            if (UpgradeWallItem.upgradeWall(wall, wallPos, e.player, e.instance) && currentLevel + 1 == targetLevel) {
                wallUpgradeQueue.removeFirst()
                e.instance.getNearbyEntities(wallPos, 0.2).onEach { if (it.hasTag(Tag.Boolean("wallUpgrade"))) it.remove() }
            }
        }
    }

    fun playerTick(instance: Instance) {
        matterExtractors.tick(this)
        val player = instance.getPlayerByUuid(UUID.fromString(uuid))
        if (player != null) {
            if (playerDisplayName == "") playerDisplayName = player.username
            updateBossBars()
        }
    }

    fun powerTick() {
        trainingCamps.tick(this)
    }

    fun mechanicalTick() {
        matterCompressors.tick(this)
    }

    fun updateBossBars(player: Player? = null) {
        powerBossBar.name("<red>$POWER_SYMBOL Power <gray>-<red> $power/$maxPower".asMini())
        powerBossBar.progress((power / maxPower).toFloat().coerceIn(0f..1f))

        matterBossBar.name("<green>$MATTER_SYMBOL Organic Matter <gray>-<green> $organicMatter/$maxMatter".asMini())
        matterBossBar.progress((organicMatter / maxMatter).toFloat().coerceIn(0f..1f))

        val overflow = disposableResourcesUsed > maxDisposableResources
        resourcesBossBar.name("<${if (overflow) "light_purple" else "aqua"}>$RESOURCE_SYMBOL Disposable Resources <gray>- <${if (overflow) "light_purple" else "aqua"}>$disposableResourcesUsed/$maxDisposableResources".asMini())
        resourcesBossBar.progress((disposableResourcesUsed.toFloat() / maxDisposableResources.toFloat()).coerceIn(0f..1f))
        resourcesBossBar.color(if (disposableResourcesUsed > maxDisposableResources) BossBar.Color.PURPLE else BossBar.Color.BLUE)

        mechanicalBossBar.name("<white>$MECHANICAL_SYMBOL Mechanical Parts<gray> - <white>$mechanicalParts".asMini())
        mechanicalBossBar.progress(((tick % 400uL).toFloat() / 400f).coerceIn(0f, 1f))
        if (player != null) {
            if (mechanicalParts > 0) player.showBossBar(mechanicalBossBar)
            else player.hideBossBar(mechanicalBossBar)
        }
    }

    private fun showBossBars(player: Player) {
        with(player) {
            showBossBar(powerBossBar)
            showBossBar(matterBossBar)
            showBossBar(resourcesBossBar)
            if (mechanicalParts > 0) {
                showBossBar(mechanicalBossBar)
            }
        }
    }

    fun setupPlayer(player: Player) {
        showBossBars(player)
        updateBossBars()
        Actionable.persistentRegistry.forEach {
            if (it is ResearchUpgradeItem) {
                if (gameInstance?.instanceConfig?.allowResearch == true) {
                    it.setItemSlot(player)
                }
            } else it.setItemSlot(player)
        }
        player.helmet = item(Material.fromNamespaceId(block.namespace())!!)
    }

    fun sendCooldowns(player: Player) {
        player.sendPackets(
            SetCooldownPacket(
                ClaimItem.itemMaterial.cooldownIdentifier,
                claimCooldown.ticks
            ),
            SetCooldownPacket(
                ColonyItem.itemMaterial.cooldownIdentifier,
                colonyCooldown.ticks
            ),
            SetCooldownPacket(
                AttackItem.itemMaterial.cooldownIdentifier,
                attackCooldown.ticks
            ),
            SetCooldownPacket(
                ClaimWaterItem.itemMaterial.cooldownIdentifier,
                raftCooldown.ticks
            )
        )
    }

    fun getBuildingReferenceByIdentifier(identifier: String): KProperty0<Building>? {
        return when(identifier) {
            "power:container" -> ::barracks
            "power:generator" -> ::trainingCamps
            "matter:container" -> ::matterContainers
            "matter:generator" -> ::matterExtractors
            "mechanical:generator" -> ::matterCompressors
            "underground:teleport" -> ::undergroundTeleporters
            else -> null
        }
    }
    val research = ResearchContainer()

    companion object {
        val NONE = PlayerData("", Block.AIR, "")
        fun Map<String, PlayerData>.getDataByBlock(block: Block): PlayerData? {
            return values.find { it.block == block }
        }
        fun Map<String, PlayerData>.getDataByPoint(point: Point, instance: Instance): PlayerData? {
            val block = instance.getBlock(point.playerPosition)
            return values.find { it.block == block}
        }
        fun Map<String, PlayerData>.toBlockSortedList(): List<PlayerData> {
            return values.sortedByDescending { it.blocks }
        }
        fun Map<String, PlayerData>.toBlockList(): List<Block> {
            return values.map { it.block }
        }
        fun genericBuildingCost(count: Int, cost: Int): Int {
            val generalCost = (count * cost) + cost
            if (generalCost > 10000) {
                return (generalCost/1000) * 1000
            } else if (generalCost > 1000) {
                return (generalCost/100) * 100
            }
            return generalCost
        }
    }
}