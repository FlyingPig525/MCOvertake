package io.github.flyingpig525

import cz.lukynka.prettylog.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.building.ToxicologyLab
import io.github.flyingpig525.command.*
import io.github.flyingpig525.console.Command
import io.github.flyingpig525.console.ConfigCommand
import io.github.flyingpig525.console.OpCommand
import io.github.flyingpig525.console.SaveCommand
import io.github.flyingpig525.data.config.Config
import io.github.flyingpig525.data.config.InstanceConfig
import io.github.flyingpig525.data.config.getCommentString
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.config.BlockConfig
import io.github.flyingpig525.data.player.config.PlayerConfig
import io.github.flyingpig525.data.player.permission.Permission
import io.github.flyingpig525.data.player.permission.PermissionManager
import io.github.flyingpig525.entity.ToxicologyLabGasEntity
import io.github.flyingpig525.item.SelectBlockItem
import io.github.flyingpig525.ksp.initBuildingCompanions
import io.github.flyingpig525.ksp.initItems
import io.github.flyingpig525.log.MCOvertakeLogType
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.bladehunt.kotstom.*
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.dsl.kbar
import net.bladehunt.kotstom.dsl.kommand.buildSyntax
import net.bladehunt.kotstom.dsl.kommand.defaultExecutor
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.get
import net.bladehunt.kotstom.extension.roundToBlock
import net.bladehunt.kotstom.extension.set
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.inventory.InventoryOpenEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType.*
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
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
const val OIL_SYMBOL = "☀"
const val PLASTIC_SYMBOL = "⏺"
const val LUBRICANT_SYMBOL = "₴"
const val SKY_SYMBOL = "INSERT SYMBOL"

const val BUILDING_INVENTORY_SLOT = 4

const val SERVER_VERSION = "v0.4.3"

const val PIXEL_SIZE = 1.0 / 16.0

const val DASH_BANNER = "----------------------------------------------"

// Used for terrain generation
val FLOWER_BLOCKS: Array<Block> = arrayOf(Block.PINK_PETALS, Block.CORNFLOWER, Block.LILY_OF_THE_VALLEY, Block.POPPY, Block.DANDELION)

var tick: ULong = 0uL

var runConsoleLoop = true
@OptIn(ExperimentalSerializationApi::class)
val json = Json { prettyPrint = true; encodeDefaults = true; allowComments = true; ignoreUnknownKeys = true; allowTrailingComma = true}

var scoreboardTitleIndex = 0

lateinit var config: Config
lateinit var parentInstanceConfig: InstanceConfig

typealias InstanceMap = MutableMap<String, GameInstance>

var instances: InstanceMap = mutableMapOf()
lateinit var lobbyInstance: InstanceContainer

lateinit var permissionManager: PermissionManager private set

val tpsMonitor = TpsMonitor()

val scoreboardTitleList = mutableListOf<Component>()
val instanceItemNameList = mutableListOf<Component>()

fun main() = runBlocking { try {
    System.setProperty("minestom.chunk-view-distance", "16")
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
        if (it is Exception) {
            log(it)
        } else {
            it.printStackTrace()
        }
    }

    val configFile = File("config.json5")
    if (!configFile.exists()) {
        configFile.createNewFile()
        configFile.writeText(getCommentString(Config()))
    }
    config = json.decodeFromString<Config>(configFile.readText())
    configFile.writeText(getCommentString(config))
    log("Config imported...", MCOvertakeLogType.FILESYSTEM)
    val parentCFile = File("parent-instance-config.json5")
    if (!parentCFile.exists()) {
        parentCFile.createNewFile()
        parentCFile.writeText(getCommentString(InstanceConfig()))
    }
    try {
        parentInstanceConfig = json.decodeFromString(parentCFile.readText())
        parentCFile.writeText(getCommentString(InstanceConfig()))
    } catch (e: Exception) {
        log("Error when loading parent instance config", LogType.ERROR)
        log(e)
    }
    val uri = object {}::class.java.getResourceAsStream("pack.zip") ?: throw Exception("Resource pack not found")
    val resourcePack = MinecraftResourcePackReader.minecraft().readFromInputStream(
        uri
    )
    if (resourcePack.description() == null) throw Exception("Resource pack not loaded")
    val builtResourcePack = MinecraftResourcePackWriter.minecraft().build(resourcePack)
    val packServer = ResourcePackServer.server()
        .address(config.serverAddress, config.packServerPort)
        .pack(builtResourcePack)
        .build()
    log("Resource pack loaded...", MCOvertakeLogType.FILESYSTEM)

    val permissionsFile = File(config.permissionFilePath)
    if (!permissionsFile.exists()) {
        permissionsFile.createNewFile()
        permissionsFile.writeText("{}")
    }
    permissionManager = json.decodeFromString(permissionsFile.readText())
    lobbyInstance = InstanceManager.createInstanceContainer().apply {
        setGenerator { unit ->
            unit.fork(Vec(0.0, -1.0, 0.0), Vec(1.0, 1.0, 1.0)).modifier().fillHeight(9, 10, Block.GRASS_BLOCK)
        }
        setChunkSupplier(::LightingChunk)
    }
    ToxicologyLabGasEntity().setInstance(lobbyInstance, Vec(3.0, 10.5, 3.0))
    log("Created lobby instance...", MCOvertakeLogType.FILESYSTEM)
    initBuildingCompanions()
    log("Building companions initialized...")
    for (name in config.instanceNames) {
        try {
            instances[name] = GameInstance(Path.of("instances", name), name)
            instances[name]?.setupInstance()
        } catch (e: Exception) {
            log("An exception occured during the setup of instance \"$name\"!", LogType.EXCEPTION)
            log(e)
        }
    }
    log("Created GameInstances")

    initItems()

    GlobalEventHandler.listen<ServerListPingEvent> {
        it.responseData.apply {
            description = "<gradient:green:gold><bold>MCOvertake - $SERVER_VERSION".asMini()
        }
    }

    GlobalEventHandler.listen<InventoryOpenEvent> {
        if (it.player.inventory == it.inventory) {
            it.player.inventory.cursorItem = ItemStack.AIR
        }
    }

    GlobalEventHandler.listen<InventoryCloseEvent> {
        it.player.inventory.cursorItem = ItemStack.AIR
    }

    GlobalEventHandler.listen<ItemDropEvent> {
        it.isCancelled = true
        it.player.data?.handAnimationWasDrop = true
        SchedulerManager.scheduleNextTick { it.player.data?.handAnimationWasDrop = false }
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
            permissionManager.addPermission(player, Permission("*"))
        }
        player.respawnPoint = Pos(8.0, 11.0, 8.0)
        val info = ResourcePackInfo.resourcePackInfo()
            .hash(builtResourcePack.hash())
            .uri(URI("http://${config.serverAddress}:${config.packServerPort}/${builtResourcePack.hash()}.zip"))
            .build()
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
            .packs(info)
            .replace(true)
            .prompt("<green>This resource pack provides \"crucial\" visual changes that allow for a better and more <i>smooth</i> experience.".asMini())
            .build()
        )

        MinecraftServer.getInstanceManager().instances.onEach {
            it.players.onEach {
                it.sendMessage("<green>+ ${player.username}".asMini())
            }
        }
    }

    GlobalEventHandler.listen<PlayerDisconnectEvent> { e ->
        MinecraftServer.getInstanceManager().instances.onEach {
            it.players.onEach {
                it.sendMessage("<red>- ${e.player.username}".asMini())
            }
        }
    }

    lobbyInstance.eventNode().listen<PlayerSpawnEvent> { e ->
        e.player.teleport(Pos(5.0, 11.0, 5.0))
        e.player.gameMode = GameMode.ADVENTURE
        e.player.inventory[0] = item(Material.COMPASS) {
            itemName = "<gradient:green:gold:$scoreboardTitleIndex><bold>MCOvertake Game Instances".asMini()
        }
        e.player.inventory.addInventoryCondition { player, slot, type, res ->
            try {
                res.isCancel = true
                val item = player.inventory[slot]
                if (item == SelectBlockItem.item) {
                    SelectBlockItem.onInteract(PlayerUseItemEvent(player, PlayerHand.MAIN, item, 0L))
                }
            } catch (_: Exception) {}
        }
        e.player.isAllowFlying = true
        e.player.isFlying = true
        e.player.removeBossBars()
    }
    lobbyInstance.eventNode().listen<PlayerSwapItemEvent> { it.isCancelled = true }
//    lobbyInstance.eventNode().listen<InventoryPreClickEvent> { it.isCancelled = true }
    lobbyInstance.eventNode().listen<ItemDropEvent> { it.isCancelled = true }
    lobbyInstance.eventNode().listen<PlayerTickEvent> { e ->
        e.player.inventory[0] =
            e.player.inventory[0].with(
                ItemComponent.ITEM_NAME,
                instanceItemNameList[scoreboardTitleIndex]
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
            instances.onEachIndexed { index, (name, instance) ->
                inventory[index] = item(Material.WHITE_WOOL) {
                    itemName = name.asMini()
                    if (instance.instanceConfig.whitelist.isNotEmpty()) {
                        lore {
                            val color = if (e.player.username in instance.instanceConfig.whitelist) "<green>" else "<red>"
                            +"${color}Whitelisted".asMini().noItalic()
                        }
                    }
                    set(Tag.String("selector"), name)
                }
            }
            inventory.addInventoryCondition { player: Player, slot: Int, clickType: ClickType, res: InventoryConditionResult ->
                res.isCancel = true
                if (res.clickedItem.hasTag(Tag.String("selector"))) {
                    val instance = instances[res.clickedItem.getTag(Tag.String("selector"))!!]
                    if (instance != null) {
                        if (instance.instanceConfig.whitelist.isNotEmpty() && player.username !in instance.instanceConfig.whitelist) {
                            player.sendMessage("<red>You are not whitelisted in this instance".asMini())
                            return@addInventoryCondition
                        }
                        if (player.instance == instance.instance) return@addInventoryCondition
                        player.inventory.clear()
                        player.closeInventory()
                        player.removeBossBars()
                        player.sendMessage("<gray>Sending you to <red>${res.clickedItem.getTag(Tag.String("selector"))}<gray>...".asMini())
                        player.instance = instance.instance
                    }
                }
            }
            e.player.openInventory(inventory)
        }
    }
    var scoreboardTitleProgress = -1.0
    // Create list of titles to free ~300 mb of ram
    while (scoreboardTitleProgress < 1.0) {
        val txt = "<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake - $SERVER_VERSION".asMini()
        if (txt !in scoreboardTitleList) {
            scoreboardTitleList += txt
            instanceItemNameList += "<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake Instance Selector".asMini()
        }
        scoreboardTitleProgress += 0.025
    }
    println(scoreboardTitleList.size)
    val bar = kbar(scoreboardTitleList[0]) {
        lobbyInstance.players.onEach { addViewer(it) }
    }
    SchedulerManager.scheduleTask({ try {
        tick++
        scoreboardTitleIndex++
        if (scoreboardTitleIndex >= scoreboardTitleList.size) {
            scoreboardTitleIndex = 0
        }
        bar.setTitle(scoreboardTitleList[scoreboardTitleIndex])
    } catch(e: Exception) {
        log("An exception occurred in the lobby scoreboard task!", LogType.EXCEPTION)
        log(e)
    }
    }, TaskSchedule.tick(1), TaskSchedule.tick(1))

    SchedulerManager.scheduleTask({ try { System.gc() } catch (e: Exception) {
        log("An exception occurred while garbage collecting!", LogType.EXCEPTION)
        log(e)
    } }, TaskSchedule.seconds(30), TaskSchedule.seconds(30))

    instances.values.onEach { it.setupSpawning() }
    log("Player spawning setup...")

    instances.values.onEach { it.setupScoreboard() }
    log("Scoreboards setup")
    instances.values.onEach { it.registerInteractionEvents() }

    // Stolen monitoring code
    tpsMonitor.start()
    val validateResearchCommand = kommand("validateResearch") {
        buildSyntax {
            condition { sender, cmd ->
                permissionManager.hasPermission(sender as Player, Permission("data.research.validation"))
            }
            executor { player, ctx ->
                (player as Player).instance.players.onEach { it.data!!.research.basicResearch.validateUpgrades() }
            }
        }
    }
    val refreshConfig = kommand("refreshConfig") {
        buildSyntax {
            condition { player, ctx ->
                permissionManager.hasPermission(player as Player, Permission("process.config.refresh"))
            }
            executor { player, ctx ->
                if (player !is Player) return@executor
                val game = player.gameInstance
                if (game == null) {
                    player.sendMessage("<red><bold>You must be in a game instance to run this command!".asMini())
                    return@executor
                }
                val data = player.data
                if (data == null /* || game.uuidParents[player.uuid.toString()] != player.uuid.toString() */) {
                    player.sendMessage("<red><bold>You must own a block to run this command!".asMini())
                    return@executor
                }
                data.blockConfig = BlockConfig()
            }
        }

    }
    val noOpCommand = kommand("removeOp") {

        buildSyntax {
            condition { player, ctx ->
                permissionManager.hasPermission(player as Player, Permission("data.research.set"))
            }
            executor { player, ctx ->
                (player as Player).data?.research?.basicResearch?.upgradeByName("Test")?.level = 0
            }
        }
    }

    val addOpCommand = kommand("addOp") {
        buildSyntax {
            condition { player, ctx ->
                permissionManager.hasPermission(player as Player, Permission("data.research.set"))
            }
            executor { player, ctx ->
                (player as Player).data?.research?.basicResearch?.upgradeByName("Test")?.level = 1
            }
        }
    }

    CommandManager.register(
        createInstanceCommand,
        lobbyCommand,
        tickCommand,
        deleteInstanceCommand,
        gcCommand,
        validateResearchCommand,
        coopCommand,
        refreshConfig,
        setTimeCommand,
        noOpCommand,
        setGrass,
        setAllCommand,
        tpCommand,
        forceInvite,
        tpAlertCommand,
        addOpCommand,
        flightSpeedCommand
    )

    // Save loop
    SchedulerManager.scheduleTask({ runBlocking { launch(Dispatchers.IO) {
            try {
                instances.values.onEach {
                    it.save()
                    System.gc()
                }
                if (config.printSaveMessages) {
                    log("Game data saved")
                }
            } catch (e: Exception) {
                log("An exception has occurred during game saving!", LogType.EXCEPTION)
                log(e)
            }
        }}
        TaskSchedule.minutes(5)
    }, TaskSchedule.minutes(5))

    instances.values.onEach { it.registerTasks() }
    log("Game loops scheduled...")

    instances.values.onEach { it.registerTickEvents() }
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
    }}.invokeOnCompletion {
        log("Console loop failed! || ${it?.cause?.message}", LogType.ERROR)
    }
    log("Console loop running!")
    if (config.printSaveMessages) {
        log("Printing save messages")
    }
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
    val y = y()
    when (y) {
        in 37.0..46.0, 5.0 -> {
            return withY(40.0)
        }
        in 28.0..36.0, 4.0 -> {
            return withY(30.0)
        }
        in 47.0..91.0, 6.0 -> {
            return withY(91.0)
        }
        else -> return withY(40.0)
    }
}

val Point.playerPosition: Point get() {
    val y = y()
    when (y) {
        in 37.0..46.0, 5.0 -> {
            return withY(5.0)
        }
        in 28.0..36.0, 4.0 -> {
            return withY(4.0)
        }
        in 47.0..91.0, 6.0 -> {
            return withY(6.0)
        }
        else -> return withY(40.0)
    }
}

val Point.visiblePosition: Point get() {
    val y = y()
    when (y) {
        in 37.0..46.0, 5.0 -> {
            return withY(39.0)
        }
        in 28.0..36.0, 4.0 -> {
            return withY(29.0)
        }
        in 47.0..91.0, 6.0 -> {
            return withY(90.0)
        }
        else -> return withY(39.0)
    }
}

val Point.isUnderground: Boolean get() {
    return y() in 28.0..36.0 || y() <= 4.0
}

val Point.isSky: Boolean get() {
    return y() >= 47 || y() == 6.0
}

fun onAllBuildingPositions(point: Point, fn: (point: Point) -> Unit) {
    fn(point.withY(91.0).buildingPosition)
    fn(point.withY(40.0).buildingPosition)
    fn(point.withY(31.0).buildingPosition)
}

fun scheduleImmediately(fn: () -> Unit) =
    SchedulerManager.scheduleTask(fn, TaskSchedule.immediate(), TaskSchedule.stop())

fun PlayerInventory.idle() {
    set(0, idleItem)
}
val idleItem = item(Material.GRAY_DYE) {
    itemName = "".asMini()
}

fun String.toUUID(): UUID? = UUID.fromString(this)

fun Point.anyAdjacentBlocksMatch(block: Block, instance: Instance): Boolean {
    var ret = false
    repeatAdjacent {
        if (instance.getBlock(it) == block.defaultState()) ret = true
    }
    return ret
}

fun Point.anyDirectionalBlocksMatch(block: Block, instance: Instance): Boolean =
    repeatDirection { point, _ -> instance.getBlock(point).defaultState() == block.defaultState() }

enum class Direction(val str: String, val opposite: String) {
    NORTH("north", "south"),
    NORTH_EAST("north_east", "south_west"),
    EAST("east", "west"),
    SOUTH_EAST("south_east", "north_west"),
    SOUTH("south", "north"),
    SOUTH_WEST("south_west", "north_east"),
    WEST("west", "east"),
    NORTH_WEST("north_west", "south_east")
}

/**
 * Calls [fn] with each adjacent location relative to this [Point] starting from the north-west going clockwise
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

/**
 * Calls [predicate] with each adjacent location, without the corners, relative to this [Point] starting from north going clockwise
 */
inline fun Point.repeatDirection(predicate: (point: Point, dir: Direction) -> Boolean): Boolean {
    if (predicate(add(0.0, 0.0, -1.0), Direction.NORTH)) return true
    if (predicate(add(1.0, 0.0, 0.0), Direction.EAST)) return true
    if (predicate(add(0.0, 0.0, 1.0), Direction.SOUTH)) return true
    return predicate(add(-1.0, 0.0, 0.0), Direction.WEST)
}

val Cooldown.ticks: Int get() = (duration.toMillis() / 50).toInt()

val Material.cooldownIdentifier: String get() = key().value()

val Player.data: BlockData? get() = instances.fromInstance(instance)?.dataResolver?.get(uuid.toString())
val Player.config: PlayerConfig? get() {
    if (instance.gameInstance == null) return null
    if (instance.gameInstance!!.playerConfigs[uuid.toString()] == null) {
        instance.gameInstance!!.playerConfigs[uuid.toString()] = PlayerConfig()
    }
    return instance.gameInstance?.playerConfigs?.get(uuid.toString())
}

fun Player.removeBossBars() {
    val bars = BossBarManager.getPlayerBossBars(this)
    for (bar in bars) {
        hideBossBar(bar)
    }
}

fun Instance.anyOnline(vararg uuids: UUID): Boolean {
    for (uuid in uuids) {
        if (getPlayerByUuid(uuid) != null) return true
    }
    return false
}

fun Instance.anyOnline(vararg uuids: String): Boolean {
    val a = uuids.map { it.toUUID()!! }.toTypedArray()
    return anyOnline(*a)
}
fun initConsoleCommands() {
    ConfigCommand
    SaveCommand
    OpCommand
}
