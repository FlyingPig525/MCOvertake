package io.github.flyingpig525

import de.articdive.jnoise.core.api.functions.Interpolation
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant
import de.articdive.jnoise.generators.noisegen.opensimplex.SuperSimplexNoiseGenerator
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import io.github.flyingpig525.console.Command
import io.github.flyingpig525.console.ConfigCommand
import io.github.flyingpig525.console.LogCommand
import io.github.flyingpig525.console.SaveCommand
import io.github.flyingpig525.data.Config
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.data.PlayerData.Companion.toBlockSortedList
import io.github.flyingpig525.item.*
import io.github.flyingpig525.wall.blockIsWall
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.InstanceManager
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.kbar
import net.bladehunt.kotstom.dsl.line
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.dsl.particle
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.roundToBlock
import net.bladehunt.kotstom.extension.set
import net.minestom.server.MinecraftServer
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.player.*
import net.minestom.server.event.trait.InventoryEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.BlockChangePacket
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.particle.Particle
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.tag.Tag
import net.minestom.server.timer.TaskSchedule
import org.slf4j.LoggerFactory
import org.slf4j.spi.SLF4JServiceProvider
import java.io.File
import java.io.PrintStream
import java.sql.Time
import java.time.Instant
import java.util.*

const val POWER_SYMBOL = "✘"
const val ATTACK_SYMBOL = "\uD83D\uDDE1"
const val MATTER_SYMBOL = "\uD83C\uDF0A"
const val MECHANICAL_PART_SYMBOL = "\uD83E\uDE93"
const val CLAIM_SYMBOL = "◎"
const val COLONY_SYMBOL = "⚐"
const val BUILDING_SYMBOL = "⧈"
const val RESOURCE_SYMBOL = "⌘"
const val WALL_SYMBOL = "\uD83E\uDE93"
const val PICKAXE_SYMBOL = "⛏"

const val DASH_BANNER = "----------------------------------------------"

var runConsoleLoop = true
val logStream = PrintStream("log.log")
val json = Json { prettyPrint = true; encodeDefaults = true }

lateinit var instance: InstanceContainer private set

lateinit var players: MutableMap<String, PlayerData>

lateinit var config: Config

fun main() = runBlocking { try {
    // Initialize the servers
    val minecraftServer = MinecraftServer.init()
    MojangAuth.init()


    val configFile = File("config.json")
    if (!configFile.exists()) {
        withContext(Dispatchers.IO) {
            configFile.createNewFile()
        }
        configFile.writeText(json.encodeToString(Config()))
    }
    config = json.decodeFromString<Config>(configFile.readText())
    configFile.writeText(json.encodeToString(config))
    log("Config imported...")

    instance = InstanceManager.createInstanceContainer().apply {
        chunkLoader = AnvilLoader("world/world")

        val noise = JNoise.newBuilder().superSimplex(SuperSimplexNoiseGenerator.newBuilder().setVariant2D(Simplex2DVariant.CLASSIC).setSeed(
            (Long.MIN_VALUE..Long.MAX_VALUE).random()
        ))
            .scale(config.noiseScale)
            .clamp(-1.0, 1.0)
            .build()
        setGenerator { unit ->
            unit.modifier().setAll { x, y, z ->
                if (x in 0..300 && z in 0..300) {
                    if (y in 38..39) return@setAll Block.GRASS_BLOCK
                }
                if (x in -1..301 && z in -1..301 && y < 40) {
                    return@setAll Block.DIAMOND_BLOCK
                }
                if (config.doNoiseTest && y == 45 && x in 0..300 && z in 0..300) {
                    val eval = noise.evaluateNoise(x.toDouble(), z.toDouble()) + if (x in 125..175 && z in 125..175) 0.07999 else 0.0
                    println(eval)
                    if (eval > config.noiseThreshold) return@setAll Block.OBSIDIAN
                }
                Block.AIR
            }
        }
        setChunkSupplier(::LightingChunk)
    }
    log("World loaded...")

    players = Json.decodeFromString<MutableMap<String, PlayerData>>(
        if (File("./player-data.json").exists())
            File("./player-data.json").readText()
        else "{}"
    )
    log("Player data imported...")
    log("Filesystem actions complete...")

    initItems()
    log("Items initialized...")
    GlobalEventHandler.listen<AsyncPlayerConfigurationEvent> { event ->
        event.spawningInstance = instance
        val player = event.player
        if (config.whitelisted.isNotEmpty() && player.username !in config.whitelisted) {
            player.kick(
                config.notWhitelistedMessage.asMini()
            )
        }
        player.respawnPoint = Pos(5.0, 41.0, 5.0)
    }


    GlobalEventHandler.listen<PlayerSpawnEvent> { e ->
        e.player.gameMode = GameMode.ADVENTURE
        e.player.flyingSpeed = 0.5f
        e.player.isAllowFlying = true
        e.player.addEffect(Potion(PotionEffect.NIGHT_VISION, 1, -1))
        val data = players[e.player.uuid.toString()]
        if (data == null) {
            SelectBlockItem.setAllSlots(e.player)
        } else {
            data.setupPlayer(e.player)
            if (e.isFirstSpawn) {
                e.player.sendPackets(
                    SetCooldownPacket(
                        ClaimItem.getItem(e.player.uuid).material().id(),
                        (data.claimCooldown.duration.toMillis() / 50).toInt()
                    ),
                    SetCooldownPacket(
                        ColonyItem.getItem(e.player.uuid).material().id(),
                        (data.colonyCooldown.duration.toMillis() / 50).toInt()
                    )
                )
            }
        }
    }
    log("Player spawning setup...")



    var scoreboardTitleProgress = -1.0
    // Scoreboard tick
    SchedulerManager.scheduleTask({
        scoreboardTitleProgress += 0.02
        if (scoreboardTitleProgress >= 1.0) {
            scoreboardTitleProgress = -1.0
        }
        val scoreboard = kbar("<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake - v0.1".asMini()) {
            for ((i, player) in players.toBlockSortedList().withIndex()) {
                if (player.playerDisplayName == null) player.playerDisplayName =
                    instance.getPlayerByUuid(player.uuid.toUUID())?.username ?: continue
                if (player.blocks == 0) continue
                line("<dark_green><bold>${player.playerDisplayName}".asMini()) {
                    isVisible = true
                    line = player.blocks
                    id = player.playerDisplayName + "$i"
                }
            }
        }
        for (player in instance.players) {
            scoreboard.addViewer(player)
        }
    }, TaskSchedule.tick(1), TaskSchedule.tick(1))


    // General player tick/extractor tick
    SchedulerManager.scheduleTask({
        for (uuid in players.keys) {
            players[uuid]!!.tick(instance)
        }
    }, TaskSchedule.tick(30), TaskSchedule.tick(30))

    // Camp tick
    SchedulerManager.scheduleTask({
        for (uuid in players.keys) {
            val data = players[uuid]!!
            data.power += data.trainingCamps.count * 0.5 + 0.5
            data.updateBossBars()
        }
    }, TaskSchedule.tick(70), TaskSchedule.tick(70))

    // Save loop
    SchedulerManager.scheduleTask({
        val file = File("./player-data.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(Json.encodeToString(players))
        instance.saveChunksToStorage()
        if (config.printSaveMessages) {
            log("Game data saved")
        }
    }, TaskSchedule.minutes(1), TaskSchedule.minutes(1))
    log("Game loops scheduled...")

    GlobalEventHandler.listen<PlayerMoveEvent> { e ->
        with(e) {
            if (newPosition.x !in 0.0..301.0 || newPosition.z !in 0.0..301.0) {
                newPosition = Pos(
                    newPosition.x.coerceIn(0.0..301.0),
                    newPosition.y,
                    newPosition.z.coerceIn(0.0..301.0),
                    newPosition.yaw,
                    newPosition.pitch
                )
            }
        }
    }

    GlobalEventHandler.listen<PlayerUseItemEvent> { e ->
        val item = e.player.getItemInHand(e.hand)
        e.isCancelled = true
        for (actionable in Actionable.registry) {
            if (item.getTag(Tag.String("identifier")) == actionable.identifier) {
                e.isCancelled = actionable.onInteract(e, instance)
                break
            }
        }
    }

    GlobalEventHandler.listen<PlayerBlockInteractEvent> { e ->
        val item = e.player.getItemInHand(e.hand)
        GlobalEventHandler.call(PlayerUseItemEvent(e.player, e.hand, item, 0))
    }
    log("Item use event registered")

    GlobalEventHandler.listen<PlayerSwapItemEvent> {
        it.isCancelled = true
    }

    GlobalEventHandler.listen<PlayerTickEvent> { e ->
        val playerData = players[e.player.uuid.toString()] ?: return@listen

        val target = e.player.getTrueTarget(20)
        if (target != null) {
            val buildingPoint = target.withY(40.0)
            val playerPoint = target.withY(39.0)
            val targetBlock = instance.getBlock(target)
            val buildingBlock = instance.getBlock(buildingPoint)
            when (val playerBlock = instance.getBlock(playerPoint)) {
                Block.GRASS_BLOCK -> {
                    val canAccess = anyAdjacentBlocksMatch(target, playerData.block)
                    if (canAccess) {
                        ClaimItem.setItemSlot(e.player)
                    } else {
                        ColonyItem.setItemSlot(e.player)
                    }
                }

                Block.DIAMOND_BLOCK -> {
                    e.player.inventory.idle()
                }

                else -> {
                    if (playerBlock == playerData.block) {
                        OwnedBlockItem.setItemSlot(e.player)
                        if (blockIsWall(targetBlock)) {
                            UpgradeWallItem.setItemSlot(e.player)
                        }
                    } else if (
                        playerBlock != playerData.block
                        && anyAdjacentBlocksMatch(playerPoint, playerData.block)
                    ) {
                        AttackItem.setItemSlot(e.player)
                    } else {
                        e.player.inventory.idle()
                    }
                }
            }
            val y40 = target.withY(40.0)
            val targetParticles = mutableListOf<SendablePacket>()
            for (i in 1..5) {
                val dustParticle = Particle.DUST.withColor(Color(25, 25, 25)).withScale(0.6f)
                targetParticles += particle {
                    particle = dustParticle
                    position = y40.add(0.2 * i, 0.0, 0.0)
                    count = 1
                    offset = Vec(0.0, 0.0, 0.0)
                }
                targetParticles += particle {
                    particle = dustParticle
                    position = y40.add(0.2 * i, 0.0, 1.0)
                    count = 1
                    offset = Vec(0.0, 0.0, 0.0)
                }
                targetParticles += particle {
                    particle = dustParticle
                    position = y40.add(0.0, 0.0, 0.2 * i)
                    count = 1
                    offset = Vec(0.0, 0.0, 0.0)
                }
                targetParticles += particle {
                    particle = dustParticle
                    position = y40.add(1.0, 0.0, 0.2 * i)
                    count = 1
                    offset = Vec(0.0, 0.0, 0.0)
                }
            }
            e.player.sendPackets(targetParticles)
        } else {
            e.player.inventory.idle()
        }
    }
    log("Player loop started...")

    GlobalEventHandler.listen<PlayerDisconnectEvent> { e ->
        val file = File("./player-data.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(Json.encodeToString(players))
        instance.saveChunksToStorage()
    }

    minecraftServer.start(config.serverAddress, config.serverPort)
    log("GameServer online!")

    initConsoleCommands()
    log("Console commands initialized...")
    launch {
        val scanner = Scanner(System.`in`)
        while (runConsoleLoop) {
            for (line in scanner) {
                var last = ""
                val args = line.split(' ').mapNotNull {
                    if (last != "") {
                        last += " $it"
                        return@mapNotNull null
                    }
                    if (it.startsWith('"')) {
                        last = it.drop(1)
                        return@mapNotNull null
                    }
                    if (it.endsWith('"')) {
                        val ret = "$last ${it.dropLast(1)}"
                        last = ""
                        return@mapNotNull ret
                    }
                    it
                }
                for (entry in Command.registry) {
                    if (entry.validate(args)) {
                        logStream.println(line)
                        entry.execute(args)
                    }
                }
            }
            delay(config.consolePollingDelay)
        }
    }

    log("Console loop running!")

} catch (e: Error) {
    e.printStackTrace()
    e.printStackTrace(logStream)
}}

fun log(msg: Any) {
    val time = Time.from(Instant.now()).toString().dropLast(9).drop(11)
    println("[$time]: $msg")
    logStream.println("[$time]: $msg")
}

fun clearBlock(block: Block) {
    scheduleImmediately {
        log("clear block")
        for (x in 0..300) {
            for (z in 0..300) {
                instance.loadChunk(Vec(x.toDouble(), z.toDouble())).thenRun {
                    if (instance.getBlock(x, 39, z) == block) {
                        instance.setBlock(x, 39, z, Block.GRASS_BLOCK)
                        instance.setBlock(x, 40, z, Block.AIR)
                    }
                }
            }
        }
    }
}

fun Entity.getTrueTarget(maxDistance: Int, onRayStep: (pos: Point) -> Unit = {}): Point? {
    val playerEyePos = position.add(0.0, eyeHeight, 0.0)
    val playerDirection = playerEyePos.direction().mul(0.5, 0.5, 0.5)
    var point = playerEyePos.asVec()
    for (i in 0..maxDistance * 2) {
        point = point.add(playerDirection)
        val block = instance.getBlock(point)
        onRayStep(point)
        if (!block.isAir) {
            val blockShape = block.registry().collisionShape()
            if (blockShape is ShapeImpl) {
                for (box in blockShape.collisionBoundingBoxes()) {
                    if (box.boundingBoxRayIntersectionCheck(
                            point.sub(playerDirection),
                            playerDirection,
                            point.asPosition().roundToBlock()
                        )
                    ) return point.roundToBlock()
                }
            }
        }
    }
    return null
}

fun scheduleImmediately(fn: () -> Unit) =
    SchedulerManager.scheduleTask(fn, TaskSchedule.immediate(), TaskSchedule.stop())

fun PlayerInventory.idle() {
    set(0, idleItem())
}

fun idleItem(): ItemStack = item(Material.GRAY_DYE) {
    itemName = "".asMini()
    amount = 1
}

fun String.toUUID(): UUID? = UUID.fromString(this)

fun anyAdjacentBlocksMatch(point: Point, block: Block): Boolean {
    for (x in (point.blockX() - 1)..(point.blockX() + 1)) {
        for (z in (point.blockZ() - 1)..(point.blockZ() + 1)) {
            if (x == point.blockX() && z == point.blockZ()) continue
            if (instance.getBlock(
                    Vec(
                        x.toDouble(),
                        point.blockY().toDouble(),
                        z.toDouble()
                    )
                ) == block
            ) {
                return true
            }
        }
    }
    return false
}

fun repeatAdjacent(point: Point, fn: (point: Point) -> Unit) {
    for (x in (point.blockX() - 1)..(point.blockX() + 1)) {
        for (z in (point.blockZ() - 1)..(point.blockZ() + 1)) {
            if (x == point.blockX() && z == point.blockZ()) continue
            fn(Vec(x.toDouble(), point.y(), z.toDouble()))
        }
    }
}

fun initItems() {
    BarracksItem
    ClaimItem
    ColonyItem
    MatterContainerItem
    MatterExtractorItem
    OwnedBlockItem
    SelectBlockItem
    SelectBuildingItem
    TrainingCampItem
    WallItem
    UpgradeWallItem
    BreakBuildingItem
}

fun initConsoleCommands() {
    ConfigCommand
    SaveCommand
    LogCommand
}