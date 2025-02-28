package io.github.flyingpig525

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant
import de.articdive.jnoise.generators.noisegen.opensimplex.SuperSimplexNoiseGenerator
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction
import de.articdive.jnoise.pipeline.JNoise
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.building.DisplayEntityBlock
import io.github.flyingpig525.building.Interactable
import io.github.flyingpig525.data.config.InstanceConfig
import io.github.flyingpig525.data.config.getCommentString
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.BlockData.Companion.getDataByBlock
import io.github.flyingpig525.data.player.BlockData.Companion.toBlockSortedList
import io.github.flyingpig525.data.player.DataResolver
import io.github.flyingpig525.data.player.config.PlayerConfig
import io.github.flyingpig525.item.*
import io.github.flyingpig525.log.MCOvertakeLogType
import io.github.flyingpig525.wall.blockIsWall
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.bladehunt.kotstom.InstanceManager
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.kbar
import net.bladehunt.kotstom.dsl.line
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.get
import net.bladehunt.kotstom.extension.set
import net.hollowcube.polar.PolarLoader
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.instance.InstanceTickEvent
import net.minestom.server.event.player.*
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.tag.Tag
import net.minestom.server.timer.TaskSchedule
import java.nio.file.Path
import java.util.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.math.pow
import kotlin.random.Random

typealias DataMap = MutableMap<String, BlockData>
typealias PlayerConfigMap = MutableMap<String, PlayerConfig>

class GameInstance(
    val path: Path,
    val name: String,
    instanceConfig: InstanceConfig = parentInstanceConfig.copy(noiseSeed = (Long.MIN_VALUE..Long.MAX_VALUE).random())
) {
    lateinit var instance: InstanceContainer

    var instanceConfig: InstanceConfig = run {
        val file = path.resolve("instance-config.json5").toFile()
        if (file.exists()) {
            return@run json.decodeFromString(file.readText())
        } else {
            return@run instanceConfig
        }
    }
        private set

    val blockData: DataMap = Json.decodeFromString<DataMap>(
        if (path.resolve("block-data.json5").toFile().exists())
            path.resolve("block-data.json5").toFile().readText()
        else "{}"
    )
    val playerConfigs: PlayerConfigMap = Json.decodeFromString(
        if (path.resolve("player-configs.json5").toFile().exists())
            path.resolve("player-configs.json5").toFile().readText()
        else "{}"
    )

    /**
     * Player uuid to block owner uuid
     *
     * ```uuidParents[uuid] = uuid key for playerData```
     */
    val uuidParents: MutableMap<String, String> =
        if (path.resolve("coop-uuids.json5").toFile().exists()) Json.decodeFromString(
            path.resolve("coop-uuids.json5").toFile().readText()
        )
        else processParents()
    // Block owner to player uuids
    val uuidParentsInverse: MutableMap<String, List<String>> get() {
        val map: MutableMap<String, List<String>> = mutableMapOf()
        for ((key, value) in uuidParents) {
            if (map[value] != null) {
                map[value] = map[value]!! + key
            } else {
                map[value] = listOf(key)
            }
        }
        return map
    }
    val dataResolver = DataResolver(this)

    // Inviter uuid to list of uuids they have sent invites to
    val outgoingCoopInvites = mutableMapOf<UUID, MutableList<Pair<UUID, String>>>()
    // Invitee uuid to list of uuids who have sent them invites
    val incomingCoopInvites = mutableMapOf<UUID, MutableList<Pair<UUID, String>>>()

    val removingPlayerBlock: MutableMap<UUID, Boolean> = mutableMapOf()

    var noOp: Boolean = false

    init {
        if (!path.exists()) {
            path.createDirectories()
        }
        val icFile = path.resolve("instance-config.json5").toFile()
        if (!icFile.exists()) {
            icFile.createNewFile()
            icFile.writeText(getCommentString(instanceConfig))
        }
        val pdFile = path.resolve("block-data.json5").toFile()
        if (!pdFile.exists()) {
            pdFile.createNewFile()
            pdFile.writeText("{}")
        }
        log("GameInstance $name created...", MCOvertakeLogType.FILESYSTEM)
    }

    fun refreshConfig() {
        val file = path.resolve("instance-config.json5").toFile()
        if (file.exists()) {
            instanceConfig = json.decodeFromString<InstanceConfig>(file.readText())
        }
    }

    private fun processParents(): MutableMap<String, String> {
        val ret = mutableMapOf<String, String>()
        blockData.keys.forEach {
            ret[it] = it
        }
        return ret
    }

    suspend fun save() = withContext(Dispatchers.IO) {
        try {
            if (!path.exists()) {
                path.createDirectories()
            }
            val icFile = path.resolve("instance-config.json5").toFile()
            if (!icFile.exists()) {
                icFile.createNewFile()
            }
            // `json` is formatted, `Json` is not
            icFile.writeText(getCommentString(instanceConfig))
            val pdFile = path.resolve("block-data.json5").toFile()
            if (!pdFile.exists()) {
                pdFile.createNewFile()
            }
            pdFile.writeText(Json.encodeToString(blockData))
            val uuidFile = path.resolve("coop-uuids.json5").toFile()
            if (!uuidFile.exists()) {
                uuidFile.createNewFile()
            }
            uuidFile.writeText(Json.encodeToString(uuidParents))
            val pCFile = path.resolve("player-configs.json5").toFile()
            if (!pCFile.exists()) {
                pCFile.createNewFile()
            }
            pCFile.writeText(Json.encodeToString(playerConfigs))
            instance.saveChunksToStorage()
            instance.saveInstance()
        } catch (e: Exception) {
            log("An exception occurred during $name instance saving!", LogType.EXCEPTION)
            log(e)
        }
    }

    fun registerTickEvents() {
        instance.eventNode().listen<PlayerTickEvent> { e ->
            val playerData = e.player.data ?: return@listen
            playerData.actionBar(e.player)
            if (playerData.showResearchTick) {
                playerData.researchTickProgress.name("<white>Research Tick <gray>-<white> ${tick % 400uL}/400".asMini())
                val perc = ((tick % 400uL).toFloat() / 400f).coerceIn(0f..1f)
                playerData.researchTickProgress.progress(perc)
                e.player.showBossBar(playerData.researchTickProgress)
            } else {
                e.player.hideBossBar(playerData.researchTickProgress)
            }

            if (e.player.position.isUnderground) {
                if (e.player.inventory[2].isAir)
                    TeleportBackItem.setItemSlot(e.player)
            } else if (!e.player.inventory[2].isAir)
                e.player.inventory[2] = ItemStack.AIR

            val target = e.player.getTrueTarget(20)
            if (target != null) {
                val buildingPoint = target.buildingPosition
                val playerPoint = target.playerPosition
                val targetBlock = instance.getBlock(target)
                val buildingBlock = instance.getBlock(buildingPoint).defaultState()
                val playerBlock = instance.getBlock(playerPoint).defaultState()
                val canAccess = playerPoint.anyAdjacentBlocksMatch(playerData.block, instance)
                val attackFromWater = playerPoint.repeatDirection { point, dir ->
                    val block = instance.getBlock(point.playerPosition).defaultState()
                    block == Block.SAND || block == Block.AIR
                }
                when (playerBlock) {
                    Block.GRASS_BLOCK -> {
                        if (canAccess) {
                            ClaimItem.setItemSlot(e.player)
                        } else {
                            ColonyItem.setItemSlot(e.player)
                        }
                    }

                    Block.DIAMOND_BLOCK -> {
                        e.player.inventory.idle()
                    }

                    Block.SAND -> {
                        if (canAccess) {
                            ClaimWaterItem.setItemSlot(e.player)
                        } else {
                            e.player.inventory.idle()
                        }
                    }

                    playerData.block -> {
                        if (blockIsWall(buildingBlock)) {
                            UpgradeWallItem.setItemSlot(e.player)
                        } else {
                            OwnedBlockItem.setItemSlot(e.player)
                        }
                    }

                    else -> {
                        val targetData = blockData.getDataByBlock(playerBlock)
                        if (canAccess) {
                            AttackItem.setItemSlot(e.player)
                        } else if (attackFromWater) {
                            AttackFromEdgeItem.setItemSlot(e.player)
                        } else if (targetData != null) {
                            PlayerItem.setItemSlot(e.player)
                        } else {
                            e.player.inventory.idle()
                        }
                    }
                }
                val y40 = target.buildingPosition.add(0.0, PIXEL_SIZE, 0.0)
                val oneZero = y40.add(1.0 - PIXEL_SIZE, 0.0, 0.0)
                val oneOne = y40.add(1.0 - PIXEL_SIZE, 0.0, 1.0 - PIXEL_SIZE)
                val zeroOne = y40.add(0.0, 0.0, 1.0 - PIXEL_SIZE)
                val targetParticles = mutableListOf<SendablePacket>()
                val color = when (playerBlock) {
                    Block.GRASS_BLOCK -> if (canAccess) Color(NamedTextColor.GREEN)
                    else Color(NamedTextColor.DARK_GREEN)

                    Block.SAND -> if (canAccess) Color(NamedTextColor.AQUA)
                    else Color(NamedTextColor.DARK_AQUA)

                    Block.DIAMOND_BLOCK -> Color(NamedTextColor.DARK_GRAY)
                    playerData.block -> Color(NamedTextColor.GOLD)
                    else -> if (canAccess) Color(NamedTextColor.RED)
                    else Color(NamedTextColor.DARK_RED)
                }
                val trailParticle = Particle.TRAIL.withColor(color).withDuration(config.targetParticleDuration)
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(oneZero), y40, Vec.ZERO, 1f, 1
                )
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(oneOne), oneZero, Vec.ZERO, 1f, 1
                )
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(zeroOne), oneOne, Vec.ZERO, 1f, 1
                )
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(y40), zeroOne, Vec.ZERO, 1f, 1
                )
                e.player.sendPackets(targetParticles)
            } else {
                e.player.inventory.idle()
            }
        }

        instance.eventNode().listen<InstanceTickEvent> { e ->
            val research = tick % 400u == 0uL
            for ((uuid, data) in blockData) {
                data.tick(e)
                if (research) {
                    try {
                        data.researchTick()
                    } catch (e: Exception) {
                        log(e)
                    }
                }
            }
        }
    }

    fun registerInteractionEvents() {
        instance.eventNode().listen<PlayerMoveEvent> { e ->
            with(e) {
                if (newPosition.x !in 0.0..instanceConfig.mapSize + 1.0 || newPosition.z !in 0.0..instanceConfig.mapSize + 1.0) {
                    newPosition = Pos(
                        newPosition.x.coerceIn(0.0..instanceConfig.mapSize + 1.0),
                        newPosition.y,
                        newPosition.z.coerceIn(0.0..instanceConfig.mapSize + 1.0),
                        newPosition.yaw,
                        newPosition.pitch
                    )
                }
            }
        }

        instance.eventNode().listen<PlayerUseItemEvent> { e ->
            e.itemUseTime = 0
            val item = e.player.getItemInHand(e.hand)
            e.isCancelled = true
            for (actionable in Actionable.registry) {
                if (item.getTag(Tag.String("identifier")) == actionable.identifier) {
                    e.isCancelled = try {
                        actionable.onInteract(e)
                    } catch (e: Exception) {
                        log(e)
                        true
                    }
                    break
                }
            }
        }

        instance.eventNode().listen<PlayerBlockInteractEvent> { e ->
            val item = e.player.getItemInHand(e.hand)
            val building = Building.getBuildingByBlock(e.block)
            var callItemUse = building?.shouldCallItemUse() ?: false
            val data = e.player.data
            if (building != null && data != null) {
                val ref = building.playerRef.get(data.buildings)
                if (ref is Interactable) {
                    ref.onInteract(e)
                }
            }
            if (callItemUse) {
                val event = PlayerUseItemEvent(e.player, e.hand, e.player.getItemInHand(e.hand), 0)
                instance.eventNode().call(event)
            }
        }
//        log("Item use event registered")

        instance.eventNode().listen<PlayerSwapItemEvent> {
            it.isCancelled = true
        }

        instance.eventNode().listen<PlayerHandAnimationEvent> {
            val item = it.player.getItemInHand(it.hand)
            for (actionable in Actionable.registry) {
                if (item.getTag(Tag.String("identifier")) == actionable.identifier) {
                    try {
                        actionable.onHandAnimation(it)
                    } catch (e: Exception) {
                        log(e)
                    }
                    break
                }
            }
        }
    }

    fun registerTasks() {
        // General player tick/matter tick
        instance.scheduler().scheduleTask({
            try {
                for (uuid in blockData.keys) {
                    val data = blockData[uuid]!!
                    data.playerTick(instance)
                }
            } catch (e: Exception) {
                log("An exception occurred in the matter tick task!", LogType.EXCEPTION)
                log(e)
            }
        }, TaskSchedule.tick(30), TaskSchedule.tick(30))

        // Power tick
        instance.scheduler().scheduleTask({
            try {
                for (uuid in blockData.keys) {
                    val data = blockData[uuid]!!
                    data.powerTick()
                    data.updateBossBars()
                }
            } catch (e: Exception) {
                log("An exception occurred in the power tick task!", LogType.EXCEPTION)
                log(e)
            }
        }, TaskSchedule.tick(70), TaskSchedule.tick(70))
    }

    fun setupScoreboard() {
        // Every tick
        SchedulerManager.scheduleTask({
            try {
                kbar("<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake - $SERVER_VERSION".asMini()) {
                    for ((i, player) in blockData.toBlockSortedList().withIndex()) {
                        if (player.playerDisplayName == "") player.playerDisplayName =
                            instance.getPlayerByUuid(player.uuid.toUUID())?.username ?: continue
                        if (player.blocks == 0) continue
                        line("<dark_green><bold>${player.playerDisplayName}".asMini()) {
                            isVisible = true
                            line = player.blocks
                            id = player.playerDisplayName + "$i"
                        }
                    }
                    instance.players.onEach { addViewer(it) }
                }
            } catch (e: Exception) {
                log("An exception occurred in the scoreboard task!", LogType.EXCEPTION)
                log(e)
            }
        }, TaskSchedule.tick(1), TaskSchedule.tick(1))
    }

    fun setupSpawning() {
        instance.eventNode().listen<PlayerSpawnEvent> { e ->
            e.player.gameMode = GameMode.ADVENTURE
            e.player.flyingSpeed = 0.5f
            e.player.isAllowFlying = true
            e.player.teleport(Pos(5.0, 41.0, 5.0))
            e.player.addEffect(Potion(PotionEffect.NIGHT_VISION, 1, -1))
            val data = e.player.data
            if (data == null) {
                SelectBlockItem.setAllSlots(e.player)
            } else {
                data.gameInstance = this
                data.setupPlayer(e.player)
                if (e.isFirstSpawn) {
                    data.sendCooldowns()
                }
            }
            e.player.config
        }
    }

    suspend fun setupInstance(player: Player? = null) = coroutineScope {
        val ambientNoise = JNoise.newBuilder()
            .white(instanceConfig.noiseSeed)
            .scale(instanceConfig.noiseScale * 100)
            .clamp(-1.0, 1.1)
            .build()
        val noise = JNoise.newBuilder()
            .superSimplex(
                SuperSimplexNoiseGenerator.newBuilder().setSeed(instanceConfig.noiseSeed).setVariant3D(
                    Simplex3DVariant.CLASSIC
                )
            )
            .octavate(2, 0.1, 1.0, FractalFunction.TURBULENCE, true)
            .scale(instanceConfig.noiseScale)
            .clamp(-1.0, 1.0)
            .build()
        val skyNoise = JNoise.newBuilder().superSimplex(
            SuperSimplexNoiseGenerator.newBuilder().setSeed(12312L).setVariant2D(
                Simplex2DVariant.CLASSIC
            )
        )
            .octavate(2, 0.1, 1.0, FractalFunction.TURBULENCE, true)
            .scale(instanceConfig.noiseScale + 0.015)
            .clamp(-1.0, 1.0)
            .build()
        instance = InstanceManager.createInstanceContainer().apply {
            chunkLoader = PolarLoader(path.resolve("world.polar"))
            val upperSkyBlocks = arrayOf(Block.DIRT, Block.COARSE_DIRT, Block.ROOTED_DIRT, Block.GRAVEL, Block.COBBLESTONE)
            val middleSkyBlocks = arrayOf(Block.DIRT, Block.STONE, Block.GRAVEL, Block.COBBLESTONE)
            val lowerSkyBlocks = arrayOf(Block.STONE, Block.STONE, Block.STONE, Block.GRAVEL, Block.COBBLESTONE)
            val upperRiverBlocks = arrayOf(Block.DIRT, Block.SAND, Block.SAND, Block.COARSE_DIRT)
            val lowerRiverBlocks = arrayOf(Block.SAND, Block.SAND, Block.SAND, Block.SANDSTONE)
            val random = Random(instanceConfig.noiseSeed)
            setGenerator { unit ->
                unit.modifier().setAll { x, y, z ->
                    if (x in 0..instanceConfig.mapSize && z in 0..instanceConfig.mapSize) {
                        val eval = noise.evaluateNoise(x.toDouble(), z.toDouble())
                        val ambientEval = ambientNoise.evaluateNoise(x.toDouble(), z.toDouble())
                        if (y == 39) {
                            if (eval > instanceConfig.noiseThreshold) return@setAll Block.GRASS_BLOCK
                        } else if (y == 5) {
                            return@setAll if (eval > instanceConfig.noiseThreshold) Block.GRASS_BLOCK else Block.SAND
                        } else if (y == 4) {
                            return@setAll if (eval > instanceConfig.noiseThreshold) Block.DIAMOND_BLOCK else Block.GRASS_BLOCK
                        } else if (y == 38) {
                            if (eval > instanceConfig.noiseThreshold) return@setAll upperRiverBlocks[random.nextInt(upperRiverBlocks.size)]
                        } else if (y == 40) {
                            if (eval > instanceConfig.noiseThreshold && ambientEval > 0.5) {
                                if (ambientEval > 0.9) {
                                    var block = FLOWER_BLOCKS[random.nextInt(FLOWER_BLOCKS.size)]
                                    if (block == Block.PINK_PETALS) block = block.withProperty("flower_amount",
                                        (random.nextInt(4) + 1).toString()
                                    )
                                    return@setAll block
                                }
                                else return@setAll Block.SHORT_GRASS
                            }
                        } else if (y in 28..35) {
                            if (eval <= instanceConfig.noiseThreshold && y != 29 && y != 35)
                                return@setAll Block.AIR

                            if (y == 29)
                                return@setAll instanceConfig.undergroundBlock
                            return@setAll Block.DEEPSLATE
                        }
                        if (y in 36..39) {
                            return@setAll if (eval > (instanceConfig.noiseThreshold - 0.05 * (y - 39.0).pow(2.0)))
                                lowerRiverBlocks[random.nextInt(lowerRiverBlocks.size)]
                            else Block.WATER
                        }
                        if (instanceConfig.generateSkyIslands) {
                            val skyEval = ((skyNoise.evaluateNoise(x.toDouble(), z.toDouble()) + 1) / 2)
                            if (skyEval > 0.77) {
                                if (y == 90) return@setAll Block.GRASS_BLOCK
                                if (y in 86..89) {
                                   if (skyEval > (0.77 + 0.01 * (y - 89.0).pow(2.0))) {
                                       when (y) {
                                           89 -> return@setAll upperSkyBlocks[random.nextInt(upperSkyBlocks.size)]
                                           88 -> return@setAll middleSkyBlocks[random.nextInt(middleSkyBlocks.size)]
                                           else -> return@setAll lowerSkyBlocks[random.nextInt(lowerSkyBlocks.size)]
                                       }
                                   }
                                }
                                if (y == 91) {
                                    if (ambientEval > 0.5) {
                                        if (ambientEval > 0.9) {
                                            var block = FLOWER_BLOCKS[random.nextInt(FLOWER_BLOCKS.size)]
                                            if (block == Block.PINK_PETALS) block = block.withProperty("flower_amount",
                                                (random.nextInt(4) + 1).toString()
                                            )
                                            return@setAll block
                                        }
                                        else return@setAll Block.SHORT_GRASS
                                    }
                                }
                                if (y == 6) return@setAll Block.GRASS_BLOCK
                            }
                        }
                    }
                    if (x in -1..instanceConfig.mapSize + 1 && z in -1..instanceConfig.mapSize + 1 && y < 40) {
                        if (y in 29..35) return@setAll Block.DEEPSLATE
                        else if (y > 24) return@setAll Block.DIAMOND_BLOCK
                        else if (y in 2..8) return@setAll Block.DIAMOND_BLOCK
                    }
                    Block.AIR
                }
            }
            setChunkSupplier(::LightingChunk)
        }
        player?.sendMessage("<green>Created instance world".asMini())
        // Player only exists on first creation through commands
        if (player == null) {
            launch {
                val displayBuildings = Building.BuildingCompanion.registry.filter { it is DisplayEntityBlock }
                for (x in 0..instanceConfig.mapSize) {
                    for (z in 0..instanceConfig.mapSize) {
                        val point = Vec(x.toDouble(), 39.0, z.toDouble())
                        instance.loadChunk(point).thenRun {
                            val playerBlock = instance.getBlock(x, 38, z)
                            if (instance.getBlock(x, 39, z) == Block.WATER && instance.getBlock(x, 38, z) != Block.SAND) {
                                ClaimWaterItem.spawnPlayerRaft(
                                    playerBlock,
                                    Vec(x.toDouble(), 40.0, z.toDouble()),
                                    instance,
                                    blockData.getDataByBlock(playerBlock)!!.uuid.toUUID()!!
                                )
                            }
                            onAllBuildingPositions(point) {
                                val playerData = blockData.getDataByBlock(instance.getBlock(it.playerPosition))
                                for (building in displayBuildings) {
                                    if ((building as DisplayEntityBlock).checkShouldSpawn(it, instance)) {
                                        (building as DisplayEntityBlock).spawn(it, instance, playerData!!.uuid.toUUID()!!)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
//            log("Spawned display entities...")
            }
        }
        instance.setTag(Tag.String("name"), name)
        instance.saveInstance()
    }

    fun totalInit(player: Player? = null) = runBlocking {
        try {
            setupInstance(player)
        } catch (e: Exception) {
            player?.sendMessage("<red>An exception occurred during instance setup!")
            log("An exception occurred during instance setup!", LogType.EXCEPTION)
            log(e)
            return@runBlocking
        }
        try {
            setupSpawning()
        } catch (e: Exception) {
            player?.sendMessage("<red>An exception occurred during spawning setup!")
            log("An exception occurred during spawning setup!", LogType.EXCEPTION)
            log(e)
            return@runBlocking
        }
        player?.sendMessage("<green>Instance spawning setup".asMini())
        try {
            setupScoreboard()
        } catch (e: Exception) {
            player?.sendMessage("<red>An exception occurred during scoreboard setup!")
            log("An exception occurred during scoreboard setup!", LogType.EXCEPTION)
            log(e)
            return@runBlocking
        }
        player?.sendMessage("<green>Instance scoreboard logic setup".asMini())
        try {
            registerInteractionEvents()
        } catch (e: Exception) {
            player?.sendMessage("<red>An exception occurred during interaction event registration!")
            log("An exception occurred during interaction event registration!", LogType.EXCEPTION)
            log(e)
            return@runBlocking
        }
        player?.sendMessage("<green>Instance events registered".asMini())
        try {
            registerTasks()
        } catch (e: Exception) {
            player?.sendMessage("<red>An exception occurred during task registration!")
            log("An exception occurred during task registration!", LogType.EXCEPTION)
            log(e)
            return@runBlocking
        }
        player?.sendMessage("<green>Instance tasks registered".asMini())
        try {
            registerTickEvents()
        } catch (e: Exception) {
            player?.sendMessage("<red>An exception occurred during tick event registration!")
            log("An exception occurred during  tick event registration!", LogType.EXCEPTION)
            log(e)
            return@runBlocking
        }
        player?.sendMessage("<green>Instance player tick handler registered".asMini())
    }

    fun clearBlock(block: Block) {
        scheduleImmediately {
            try {
                for (x in 0..instanceConfig.mapSize) {
                    for (z in 0..instanceConfig.mapSize) {
                        instance.loadChunk(Vec(x.toDouble(), z.toDouble())).thenRun {
                            onAllBuildingPositions(Vec(x.toDouble(), 1.0, z.toDouble())) {
                                if (instance.getBlock(it.playerPosition) == block) {
                                    if (instance.getBlock(it.visiblePosition) == Block.WATER) {
                                        ClaimWaterItem.destroyPlayerRaft(it.buildingPosition, instance)
                                        instance.setBlock(it.playerPosition, Block.SAND)
                                    } else {
                                        instance.setBlock(it.visiblePosition, Block.GRASS_BLOCK)
                                        instance.setBlock(it.playerPosition, Block.GRASS_BLOCK)
                                    }
                                    instance.setBlock(it.buildingPosition, Block.AIR)
                                    instance.getNearbyEntities(it.buildingPosition, 0.2).onEach {
                                        if (it !is Player) it.remove()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log("An error occurred during block clearing!", LogType.EXCEPTION)
                log(e)
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun delete() {
        try {
            if (path.exists()) {
                path.deleteRecursively()
            }
        } catch (e: Exception) {
            log("An exception occurred during instance deletion!", LogType.EXCEPTION)
            log(e)
        }
    }

    fun updateConfig() {
        instanceConfig = run {
            val file = path.resolve("instance-config.json5").toFile()
            if (file.exists()) {
                return@run json.decodeFromString(file.readText())
            } else {
                return@run parentInstanceConfig.copy(noiseSeed = (Long.MIN_VALUE..Long.MAX_VALUE).random())
            }
        }
    }

    operator fun get(uuid: UUID) = dataResolver[uuid]
    operator fun get(uuid: String) = dataResolver[uuid]

    companion object {
        fun InstanceMap.fromInstance(instance: Instance): GameInstance? {
            // Random string so it doesn't return anything on accident
            return this[instance.getTag(Tag.String("name")) ?: return null]
        }

        val Entity.gameInstance get() = instance.gameInstance
        val Instance.gameInstance get() = instances.fromInstance(this)
    }
}