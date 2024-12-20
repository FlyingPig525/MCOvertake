package io.github.flyingpig525

import cz.lukynka.prettylog.*
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant
import de.articdive.jnoise.generators.noisegen.opensimplex.SuperSimplexNoiseGenerator
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction
import de.articdive.jnoise.pipeline.JNoise
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.*
import io.github.flyingpig525.console.Command
import io.github.flyingpig525.console.ConfigCommand
import io.github.flyingpig525.console.OpCommand
import io.github.flyingpig525.console.SaveCommand
import io.github.flyingpig525.data.Config
import io.github.flyingpig525.data.InstanceConfig
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.item.*
import io.github.flyingpig525.log.MCOvertakeLogType
import io.github.flyingpig525.wall.blockIsWall
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.bladehunt.kotstom.*
import net.bladehunt.kotstom.command.Kommand
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.kbar
import net.bladehunt.kotstom.dsl.kommand.buildSyntax
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.get
import net.bladehunt.kotstom.extension.roundToBlock
import net.bladehunt.kotstom.extension.set
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.color.Color
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType.*
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.tag.Tag
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.time.Cooldown
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.server.ResourcePackServer
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.util.*


const val POWER_SYMBOL = "✘"
const val ATTACK_SYMBOL = "\uD83D\uDDE1"
const val MATTER_SYMBOL = "\uD83C\uDF0A"
const val MECHANICAL_SYMBOL = "☵"
const val CLAIM_SYMBOL = "◎"
const val COLONY_SYMBOL = "⚐"
const val BUILDING_SYMBOL = "⧈"
const val RESOURCE_SYMBOL = "⌘"
const val WALL_SYMBOL = "\uD83E\uDE93"
const val PICKAXE_SYMBOL = "⛏"
const val GLOBAL_RESEARCH_SYMBOL = "\uD83E\uDDEA"

const val SERVER_VERSION = "v0.3"

const val PIXEL_SIZE = 1.0 / 16.0

const val DASH_BANNER = "----------------------------------------------"

var tick: ULong = 0uL

var runConsoleLoop = true
@OptIn(ExperimentalSerializationApi::class)
val json = Json { prettyPrint = true; encodeDefaults = true; allowComments = true;}

var scoreboardTitleProgress = -1.0

lateinit var config: Config
lateinit var parentInstanceConfig: InstanceConfig

var instances: MutableMap<String, GameInstance> = mutableMapOf()

fun main() = runBlocking { try {
    LoggerSettings.saveToFile = true
    LoggerSettings.saveDirectoryPath = "./logs/"
    LoggerSettings.logFileNameFormat = "yyyy-MM-dd-Hms"
    LoggerSettings.loggerStyle = LoggerStyle.PREFIX
    LoggerFileWriter.load()
    // Initialize the servers
    val minecraftServer = MinecraftServer.init()
    MojangAuth.init()
    MinecraftServer.setBrandName("MCOvertake")
    MinecraftServer.getExceptionManager().setExceptionHandler {
        log(it as Exception)
    }

    val configFile = File("config.json")
    if (!configFile.exists()) {
        configFile.createNewFile()
        configFile.writeText(json.encodeToString(Config()))
    }
    config = json.decodeFromString<Config>(configFile.readText())
    configFile.writeText(json.encodeToString(config))
    log("Config imported...", MCOvertakeLogType.FILESYSTEM)
    val parentCFile = File("parent-instance-config.json")
    if (!parentCFile.exists()) {
        parentCFile.createNewFile()
        parentCFile.writeText(json.encodeToString(InstanceConfig()))
    }
    parentInstanceConfig = json.decodeFromString(parentCFile.readText())
    parentCFile.writeText(json.encodeToString(InstanceConfig()))

    val resourcePack = MinecraftResourcePackReader.minecraft().readFromZipFile(File("res/pack.zip"))
    val builtResourcePack = MinecraftResourcePackWriter.minecraft().build(resourcePack)
    val packServer = ResourcePackServer.server()
        .address(config.serverAddress, config.packServerPort)
        .pack(builtResourcePack)
        .build()
    log("Resource pack loaded...", MCOvertakeLogType.FILESYSTEM)

    val lobbyInstance = InstanceManager.createInstanceContainer().apply {
        setGenerator { unit ->
            unit.modifier().fillHeight(9, 10, Block.GRASS_BLOCK)
        }
        setChunkSupplier(::LightingChunk)
    }
    log("Created lobby instance...", MCOvertakeLogType.FILESYSTEM)
    for (name in config.instanceNames) {
        instances[name] = GameInstance(Path.of("instances", name), name)
        instances[name]?.setupInstance()
    }
    log("Created GameInstances")
    initBuildingCompanions()
    log("Building companions initialized...")

    initItems()


    GlobalEventHandler.listen<ServerListPingEvent> {
        it.responseData.apply {
            description = "<gradient:green:gold><bold>MCOvertake - $SERVER_VERSION".asMini()
        }
    }

    GlobalEventHandler.listen<AsyncPlayerConfigurationEvent> { event ->
        event.spawningInstance = lobbyInstance
        val player = event.player
        if (config.whitelisted.isNotEmpty() && player.username !in config.whitelisted) {
            player.kick(
                config.notWhitelistedMessage.asMini()
            )
        }
        if (player.username in config.opUsernames && player.uuid.toString() in config.opUUID) {
            player.permissionLevel = 4
        }
        player.respawnPoint = Pos(5.0, 11.0, 5.0)
        player.sendResourcePacks(ResourcePackInfo.resourcePackInfo()
            .hash(builtResourcePack.hash())
            .uri(URI("http://${config.serverAddress}:${config.packServerPort}/${builtResourcePack.hash()}.zip"))
            .build()
        )
    }

    lobbyInstance.eventNode().listen<PlayerSpawnEvent> { e ->
        e.player.teleport(Pos(5.0, 11.0, 5.0))
        e.player.isAllowFlying = true
        e.player.inventory[0] = item(Material.COMPASS) {
            itemName = "<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake Game Instances".asMini()
        }
    }
    lobbyInstance.eventNode().listen<PlayerSwapItemEvent> { it.isCancelled = true }
    lobbyInstance.eventNode().listen<InventoryClickEvent> { it.cancel() }
    lobbyInstance.eventNode().listen<ItemDropEvent> { it.isCancelled = true }
    lobbyInstance.eventNode().listen<PlayerTickEvent> { e ->
        e.player.inventory[0] =
            e.player.inventory[0].with(
                ItemComponent.ITEM_NAME,
                "<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake Game Instances".asMini()
            )
    }
    lobbyInstance.eventNode().listen<PlayerUseItemEvent> { e ->
        if (e.itemStack.material() == Material.COMPASS) {
            val type = with(instances) {
                if (size <= 9) CHEST_1_ROW
                else if (size <= 18) CHEST_2_ROW
                else if (size <= 27) CHEST_3_ROW
                else if (size <= 36) CHEST_4_ROW
                else if (size <= 45) CHEST_5_ROW
                else CHEST_6_ROW
            }
            val inventory = Inventory(type, "Game Instances")
            instances.onEachIndexed { index, (name, _) ->
                inventory[index] = item(Material.WHITE_WOOL) {
                    itemName = name.asMini()
                    set(Tag.String("selector"), name)
                }
            }
            e.player.openInventory(inventory)
            val node = EventNode.event(
                "instance-picker${e.player.uuid.mostSignificantBits}",
                EventFilter.INSTANCE)
            { it.instance == lobbyInstance }
            node.listen<InventoryClickEvent> {
                it.cancel()
                if (it.inventory != inventory) {
                    lobbyInstance.eventNode().removeChildren("instance-picker${e.player.uuid.mostSignificantBits}")
                }
                if (it.clickType != ClickType.LEFT_CLICK) return@listen


                if (it.clickedItem.hasTag(Tag.String("selector"))) {
                    val instance = instances[it.clickedItem.getTag(Tag.String("selector")) ?: "auhdiauowhd2y0189dh7278dhw89dh7 2"]
                    if (instance != null) {
                        if (it.player.instance == instance.instance) return@listen
                        lobbyInstance.eventNode().removeChildren("instance-picker${e.player.uuid.mostSignificantBits}")
                        it.player.inventory.clear()
                        it.player.closeInventory()
                        it.player.removeBossBars()
                        it.player.sendMessage("<gray>Sending you to <red>${it.clickedItem.getTag(Tag.String("selector"))}<gray>...".asMini())
                        it.player.instance = instance.instance
                    }
                }
            }
            lobbyInstance.eventNode().addChild(node)
        }
    }

    SchedulerManager.scheduleTask({
        tick++

        scoreboardTitleProgress += 0.02
        if (scoreboardTitleProgress >= 1.0) {
            scoreboardTitleProgress = -1.0
        }
        kbar("<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake - $SERVER_VERSION".asMini()) {
            lobbyInstance.players.onEach { addViewer(it) }
        }
    }, TaskSchedule.tick(1), TaskSchedule.tick(1))

    SchedulerManager.scheduleTask({ System.gc() }, TaskSchedule.seconds(30), TaskSchedule.seconds(30))

    instances.values.onEach { it.setupSpawning() }
    log("Player spawning setup...")

    instances.values.onEach { it.setupScoreboard() }
    log("Scoreboards setup")
    instances.values.onEach { it.registerInteractionEvents() }

    // Stolen monitoring code
    val tpsMonitor = TpsMonitor()
    tpsMonitor.start()
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
    val lobbyCommand = Kommand("lobby", "hub", "s", "home").apply {
        defaultExecutor = CommandExecutor { sender, context ->
            if (sender !is Player) return@CommandExecutor
            if (sender.instance == lobbyInstance) return@CommandExecutor
            sender.closeInventory()
            sender.inventory.clear()
            sender.instance = lobbyInstance
            sender.removeBossBars()
            sender.sendMessage("<gray>Sending you to the lobby...".asMini())
        }
    }
    val createInstanceCommand = kommand {
        name = "createInstance"
        buildSyntax(ArgumentString("name")) {
            executorAsync {
                val name = context.getRaw("name")
                try {
                    if (name == "") {
                        player.sendMessage("<red><bold>Invalid instance name!".asMini())
                        return@executorAsync
                    }
                    if (name in instances.keys) {
                        player.sendMessage("<red><bold>Instance \"$name\" already exists!".asMini())
                        return@executorAsync
                    }
                    instances[name] = GameInstance(Path.of("instances", name), name).apply { totalInit() }
                    config.instanceNames += name
                    configFile.writeText(json.encodeToString(config))
                    player.sendMessage("<green><bold>Created instance \"$name\" successfully!".asMini())
                } catch(e: Exception) {
                    // IllegalPathException doesnt exist but it does??????
                    if (e::class.simpleName == "IllegalPathException") {
                        player.sendMessage("<red><bold>Name \"$name\" contains illegal characters!".asMini())
                    }
                    player.sendMessage("<red><bold>Something went wrong! </bold>(${e::class.simpleName} || ${e.message})".asMini())
                    log(e)
                }
            }
        }
    }
    val deleteInstanceCommand = kommand {
        name = "removeInstance"
        val argument = ArgumentString("name").apply {
            setSuggestionCallback { sender, context, suggestion ->
                for (name in instances.keys) {
                    val current = context.getRaw("name")
                    if (current in name) {
                        suggestion.addEntry(SuggestionEntry(name))
                    }
                }
                if (suggestion.entries.size == 0) {
                    suggestion.entries += instances.keys.map { SuggestionEntry(it) }
                }
            }
        }
        buildSyntax(argument) {
            onlyPlayers()
            executorAsync {
                val name = get(argument)
                if (name !in instances.keys) {
                    player.sendMessage("<red><bold>Instance \"$name\" does not exist!".asMini())
                }
                val gameInstance = instances[name]!!
                if (!gameInstance.instance.players.isEmpty()) {
                    player.sendMessage("<red><bold>Cannot delete an instance with players!".asMini())
                }
                InstanceManager.unregisterInstance(gameInstance.instance)
                gameInstance.delete()
                instances.remove(name)
                config.instanceNames.remove(name)
                configFile.writeText(json.encodeToString(config))
                player.sendMessage("<green><bold>Instance \"$name\" successfully removed!".asMini())
                System.gc()
            }
        }
    }
    val gcCommand = kommand {
        name = "gc"
        defaultExecutor {
            var ramUsage = (BenchmarkManager.usedMemory / 1e6).toLong()
            player.sendMessage("Before: ${ramUsage}mb")
            System.gc()
            ramUsage = (BenchmarkManager.usedMemory / 1e6).toLong()
            player.sendMessage("After: ${ramUsage}mb")
        }
    }
    CommandManager.register(createInstanceCommand, lobbyCommand, tickCommand, deleteInstanceCommand, gcCommand)

    // Save loop
    SchedulerManager.scheduleTask({
        instances.values.onEach { it.save() }
        if (config.printSaveMessages) {
            log("Game data saved")
        }
    }, TaskSchedule.minutes(1), TaskSchedule.minutes(1))

    instances.values.onEach { it.registerTasks() }
    log("Game loops scheduled...")

    instances.values.onEach { it.registerTickEvent() }
    log("Player loop started...")


    minecraftServer.start(config.serverAddress, config.serverPort)
    log("GameServer online!")
    packServer.start()
    log("PackServer online!")

    initConsoleCommands()
    log("Console commands initialized...")
    launch { runBlocking {
        val scanner = Scanner(System.`in`)
        while (runConsoleLoop) {
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                var last = ""
                val args = line.split(' ').mapNotNull {
                    if (it.startsWith('"')) {
                        if (it.endsWith('"')) return@mapNotNull it.drop(1).dropLast(1)
                        last = it.drop(1)
                        return@mapNotNull null
                    }
                    if (it.endsWith('"')) {
                        val ret = "$last ${it.dropLast(1)}"
                        last = ""
                        return@mapNotNull ret
                    }
                    if (last != "") {
                        last += " $it"
                        return@mapNotNull null
                    }
                    it
                }
                for (entry in Command.registry) {
                    if (entry.validate(args)) {
                        LoggerFileWriter.writeToFile(line, LogType.USER_ACTION)
                        entry.execute(args)
                    }
                }
            }
            delay(config.consolePollingDelay)
        }
        log("Console loop failed!", LogType.ERROR)
    }}
    log("Console loop running!")
} catch (e: Exception) {
    log(e)
}}


fun Entity.getTrueTarget(maxDistance: Int, onRayStep: ((pos: Point, block: Block) -> Unit)? = null): Point? {
    val playerEyePos = position.add(0.0, eyeHeight, 0.0)
    val playerDirection = playerEyePos.direction().mul(0.5, 0.5, 0.5)
    var point = playerEyePos.asVec()
    for (i in 0..maxDistance * 2) {
        point = point.add(playerDirection)
        val block = instance.getBlock(point)
        if (onRayStep != null) onRayStep(point, block)
        if (block.defaultState() == Block.WATER) return point.roundToBlock()
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

val Point.buildingPosition: Point get() {
    // TODO: WHEN ADDING DIFFERENT LEVELS ADD MORE CASES
    if (y() in 38.0..46.0) {
        return withY(40.0)
    }
    if (y() in 29.0..37.0) {
        return withY(31.0)
    }
    return withY(40.0)
}

val Point.playerPosition: Point get() {
    if (y() in 38.0..46.0) {
        return withY(38.0)
    }
    if (y() in 29.0..37.0) {
        return withY(29.0)
    }
    return withY(38.0)
}

val Point.visiblePosition: Point get() {
    if (y() in 38.0..46.0) {
        return withY(39.0)
    }
    if (y() in 29.0..37.0) {
        return withY(30.0)
    }
    return withY(39.0)
}

val Point.isUnderground: Boolean get() {
    return y() <= 37.0
}

fun onAllBuildingPositions(point: Point, fn: (point: Point) -> Unit) {
    fn(point.withY(40.0))
    fn(point.withY(31.0))
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

fun Point.anyAdjacentBlocksMatch(block: Block, instance: Instance): Boolean {
    var ret = false
    repeatAdjacent {
        if (instance.getBlock(it) == block.defaultState()) ret = true
    }
    return ret
}

/**
 * Calls [fn] with each adjacent location relative to [point] starting from the north-west going clockwise
 */
inline fun Point.repeatAdjacent(fn: (point: Point) -> Unit) {
    fn(add(-1.0, 0.0, -1.0))
    fn(add(0.0, 0.0, -1.0))
    fn(add(1.0, 0.0, -1.0))
    fn(add(1.0, 0.0, 0.0))
    fn(add(1.0, 0.0, 1.0))
    fn(add(0.0, 0.0, 1.0))
    fn(add(-1.0, 0.0, 1.0))
    fn(add(-1.0, 0.0, 0.0))
}

var Entity.hasGravity: Boolean
    get() = !this.hasNoGravity()
    set(value) = this.setNoGravity(!value)

val Cooldown.ticks: Int get() = (duration.toMillis() / 50).toInt()

val Material.cooldownIdentifier: String get() = key().value()

val Player.data: PlayerData? get() = instances.fromInstance(instance)?.playerData?.get(uuid.toString())

fun InventoryClickEvent.cancel() {
    inventory[slot] = clickedItem
    player.inventory.cursorItem = cursorItem
}

fun Player.removeBossBars() {
    val bars = BossBarManager.getPlayerBossBars(this)
    for (bar in bars) {
        hideBossBar(bar)
    }
}

fun initItems() {
    AttackItem
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
    ClaimWaterItem
    MatterCompressorItem
    UndergroundTeleporterItem
    TeleportBackItem
    ResearchUpgradeItem
}

fun initBuildingCompanions() {
    Barrack.BarrackCompanion
    MatterCompressionPlant.MatterCompressionPlantCompanion
    MatterContainer.MatterContainerCompanion
    MatterExtractor.MatterExtractorCompanion
    UndergroundTeleporter.UndergroundTeleporterCompanion
}

fun initConsoleCommands() {
    ConfigCommand
    SaveCommand
    OpCommand
}

