package io.github.flyingpig525.data.player

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.building.lubricantColor
import io.github.flyingpig525.building.plasticColor
import io.github.flyingpig525.data.player.config.BlockConfig
import io.github.flyingpig525.data.research.ResearchContainer
import io.github.flyingpig525.item.*
import io.github.flyingpig525.ksp.PlayerBuildings
import io.github.flyingpig525.serialization.BlockSerializer
import io.github.flyingpig525.wall.wallLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.event.instance.InstanceTickEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.ActionBarPacket
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.tag.Tag
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.Instant
import java.util.*

@Serializable
class BlockData(val uuid: String, @Serializable(BlockSerializer::class) val block: Block, var playerDisplayName: String) {
    @Transient var gameInstance: GameInstance? = null
    var blockConfig = BlockConfig()
    var blocks: Int = 0
    val buildings = PlayerBuildings()
    val maxPower: Int get() = 100 + buildings.barracks.count * 25
    val maxMatter: Int get() = 100 + buildings.matterContainers.count * 25
    val claimCost: Int get() = blocks.floorDiv(500) + 5
    val maxClaimCooldown get() = (((blocks.toLong() / 1000.0) * 50.0) + 1000.0).toLong()
    val colonyCost: Int get() = claimCost * 10
    val raftCost: Int get() = (blocks.floorDiv(10000) * 500) + 500
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
            if (value != 0) hasUnlockedMechanicalParts = true
        }
    var plastic: Int = 0
        set(value) {
            field = value
            if (value != 0) hasUnlockedPlastic = true
        }
    var lubricant: Int = 0
        set(value) {
            field = value
            if (value != 0) hasUnlockedLubricant = true
        }
    val disposableResourcesUsed: Int get() {
        var acc = 0
        for (building in Building.BuildingCompanion.registry) {
                acc += building.playerRef.get(buildings).resourceUse
        }
        return acc
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
    @Transient val researchTickProgress = BossBar.bossBar(
        "<white>Research Tick <gray>-<white> ${tick % 400uL}/400".asMini(),
        0f,
        BossBar.Color.YELLOW,
        BossBar.Overlay.NOTCHED_20
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
    @Transient var sunOrMoonChangeCooldown: Cooldown = Cooldown(Duration.ZERO)
    var hasUnlockedMechanicalParts = false
    var hasUnlockedPlastic = false
    var hasUnlockedLubricant = false
    val showResearchTick get() = hasUnlockedMechanicalParts || hasUnlockedPlastic || hasUnlockedLubricant

    fun tick(e: InstanceTickEvent) {
        if (wallUpgradeQueue.isNotEmpty() && wallUpgradeCooldown.isReady(Instant.now().toEpochMilli())) run {
            val (wallPos, targetLevel) = wallUpgradeQueue.first()
            if (e.instance.getBlock(wallPos.playerPosition) != block) return@run
            val wall = e.instance.getBlock(wallPos)
            val currentLevel = wall.wallLevel
            if (currentLevel == 0 || currentLevel >= targetLevel) {
                wallUpgradeQueue.removeFirst()
                e.instance.getNearbyEntities(wallPos, 0.2).onEach { it.remove() }
                return@run
            }
            if (UpgradeWallItem.upgradeWall(wall, wallPos, this, e.instance) && currentLevel + 1 == targetLevel) {
                wallUpgradeQueue.removeFirst()
                e.instance.getNearbyEntities(wallPos, 0.2).onEach { if (it.hasTag(Tag.Boolean("wallUpgrade"))) it.remove() }
            }
        }
    }

    fun playerTick(instance: Instance) {
        buildings.matterExtractors.tick(this)
        buildings.rockMiners.tick(this)
        val player = instance.getPlayerByUuid(UUID.fromString(uuid))
        if (player != null) {
            if (playerDisplayName == "") playerDisplayName = player.username
            updateBossBars()
        }
    }

    fun powerTick() {
        buildings.trainingCamps.tick(this)
        buildings.armsManufacturers.tick(this)
    }

    fun researchTick() {
        buildings.matterCompressors.tick(this)
        buildings.basicResearchStations.tick(this)
        buildings.plasticPlants.tick(this)
        buildings.lubricantProcessors.tick(this)
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

    }

    fun actionBar(player: Player) {
        var str = "<dark_gray>| ".asMini()
        fun AAA() { str = str.append(" <reset><dark_gray>| ".asMini()) }
        if (hasUnlockedMechanicalParts) {
            str = str.append("<white>$MECHANICAL_SYMBOL <bold>$mechanicalParts".asMini())
            AAA()
        }
        if (hasUnlockedPlastic) {
            str = str.append("$plasticColor$PLASTIC_SYMBOL <bold>$plastic".asMini())
            AAA()
        }
        if (hasUnlockedLubricant) {
            str = str.append("$lubricantColor$LUBRICANT_SYMBOL <bold>$lubricant".asMini())
            AAA()
        }
        // for each intermediary resource run AAA() before appending it
        player.sendPacket(ActionBarPacket(str))
    }

    private fun showBossBars(player: Player) {
        with(player) {
            showBossBar(powerBossBar)
            showBossBar(matterBossBar)
            showBossBar(resourcesBossBar)
            if (showResearchTick) {
                showBossBar(researchTickProgress)
            } else {
                hideBossBar(researchTickProgress)
            }
        }
    }

    fun setupPlayer(player: Player) {
        showBossBars(player)
        updateBossBars()
        player.inventory.clear()
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
        sendPackets(
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

    fun sendPacket(packet: SendablePacket) {
        gameInstance!!.uuidParentsInverse[uuid]?.map {
            gameInstance!!.instance.getEntityByUuid(it.toUUID()) as Player?
        }?.onEach { it?.sendPacket(packet) }
    }

    fun sendPackets(vararg packets: SendablePacket) {
        packets.forEach { sendPacket(it) }
    }

    val research = ResearchContainer()

    companion object {
        val NONE = BlockData("", Block.AIR, "")
        fun Map<String, BlockData>.getDataByBlock(block: Block): BlockData? {
            return values.find { it.block == block }
        }
        fun Map<String, BlockData>.getDataByPoint(point: Point, instance: Instance): BlockData? {
            val block = instance.getBlock(point.playerPosition)

            return values.find { it.block == block}
        }
        fun Map<String, BlockData>.toBlockSortedList(): List<BlockData> {
            return values.sortedByDescending { it.blocks }
        }
        fun Map<String, BlockData>.toBlockList(): List<Block> {
            return values.map { it.block }
        }
    }
}