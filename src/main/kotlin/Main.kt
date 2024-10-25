package io.github.flyingpig525

import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.data.PlayerData.Companion.toBlockSortedList
import io.github.flyingpig525.item.*
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
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.bladehunt.kotstom.util.KBar
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.event.instance.InstanceTickEvent
import net.minestom.server.event.player.*
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.timer.TaskSchedule
import java.io.File
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

const val POWER_SYMBOL = "✘"
const val ATTACK_SYMBOL = "\uD83D\uDDE1"
const val MATTER_SYMBOL = "\uD83C\uDF0A"
const val MECHANICAL_PART_SYMBOL = "\uD83E\uDE93"
const val CLAIM_SYMBOL = "◎"
const val COLONY_SYMBOL = "⚐"
const val BUILDING_SYMBOL = "⧈"
const val RESOURCE_SYMBOL = "⌘"

lateinit var instance: InstanceContainer private set

lateinit var players: MutableMap<String, PlayerData> private set

fun main() {
    // Initialize the server
    val minecraftServer = MinecraftServer.init()
    // TODO: dont forget to turn this back on
    MojangAuth.init()
//    MinecraftServer.getBrandName()

    initItems()

    instance = InstanceManager.createInstanceContainer().apply {
        chunkLoader = AnvilLoader("world/world")

        setGenerator { unit ->
            unit.modifier().setAll { x, y, z ->
                if (x in 0..300 && y == 39 && z in 0..300) {
                    return@setAll Block.GRASS_BLOCK
                }
                if (x in -1..301 && y == 39 && z in -1..301) {
                    return@setAll Block.DIAMOND_BLOCK
                }
                Block.AIR
            }
        }
        setChunkSupplier(::LightingChunk)
    }

    players = Json.decodeFromString<MutableMap<String, PlayerData>>(
        if (File("./player-data.json").exists())
            File("./player-data.json").readText()
        else "{}"
    )


    GlobalEventHandler.listen<AsyncPlayerConfigurationEvent> { event ->
        event.spawningInstance = instance
        val player = event.player
        player.respawnPoint = Pos(5.0, 40.0, 5.0)
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

    var scoreboardTitleProgress = -1.0
    // Scoreboard tick
    SchedulerManager.scheduleTask({
        scoreboardTitleProgress += 0.02
        if (scoreboardTitleProgress >= 1.0) {
            scoreboardTitleProgress = -1.0
        }
        val scoreboard =  kbar("<gradient:green:gold:$scoreboardTitleProgress><bold>MCOvertake - v0.1".asMini()) {
            for ((i, player) in players.toBlockSortedList().withIndex()) {
                if (player.playerDisplayName == null) continue
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
        val uuid = e.player.uuid
        e.isCancelled = true
        if (item == SelectBlockItem.getItem(uuid)) {
            e.isCancelled = SelectBlockItem.onInteract(e, instance)
            return@listen
        }
        for (actionable in Actionable.registry) {
            if (item == actionable.getItem(uuid)) {
                e.isCancelled = actionable.onInteract(e, instance)
                break
            }
        }
    }

    GlobalEventHandler.listen<PlayerBlockInteractEvent> { e ->
        val item = e.player.getItemInHand(e.hand)
        GlobalEventHandler.call(PlayerUseItemEvent(e.player, e.hand, item, 0))
    }

    GlobalEventHandler.listen<PlayerSwapItemEvent> {
        it.isCancelled = true
    }

    GlobalEventHandler.listen<PlayerTickEvent> { e ->
        val playerData = players[e.player.uuid.toString()] ?: return@listen

        val target = e.player.getTargetBlockPosition(20)
        if (target != null) {
            var block = instance.getBlock(target)
            repeat(2) {
                if (block == Block.AIR) return@repeat
                when (block) {
                    Block.GRASS_BLOCK -> {
                        var canAccess = false
                        for (x in (target.blockX() - 1)..(target.blockX() + 1)) {
                            for (z in (target.blockZ() - 1)..(target.blockZ() + 1)) {
                                if (x == target.blockX() && z == target.blockZ()) continue
                                if (instance.getBlock(
                                        Vec(
                                            x.toDouble(),
                                            target.blockY().toDouble(),
                                            z.toDouble()
                                        )
                                    ) == playerData.block
                                ) {
                                    canAccess = true
                                    break
                                }
                            }
                            if (canAccess) break
                        }
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
                        if (block == playerData.block) {
                            OwnedBlockItem.setItemSlot(e.player)
                        } else if (block == Block.DIAMOND_BLOCK) {
                            e.player.inventory.idle()
                        } else if (instance.getBlock(target.sub(0.0, 1.0, 0.0)) != playerData.block) {
                            AttackItem.setItemSlot(e.player)
                        }
                    }
                }
                block = instance.getBlock(target.sub(0.0, 1.0, 0.0))
            }
        } else {
            e.player.inventory.idle()
        }

    }

    GlobalEventHandler.listen<PlayerDisconnectEvent> { e ->
        val file = File("./player-data.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(Json.encodeToString(players))
        instance.saveChunksToStorage()
    }

    minecraftServer.start("0.0.0.0", 25565)
}

fun clearBlock(block: Block) {
    scheduleImmediately {
        println("clear block")
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

fun scheduleImmediately(fn: () -> Unit) = SchedulerManager.scheduleTask(fn, TaskSchedule.immediate(), TaskSchedule.stop())

fun PlayerInventory.idle() {
    set(0, idleItem())
}

fun idleItem(): ItemStack = item(Material.GRAY_DYE) {
    itemName = "".asMini()
    amount = 1
}

fun String.toUUID(): UUID? = UUID.fromString(this)

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
}