package io.github.flyingpig525

import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.item.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.InstanceManager
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.listen
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.FeatureFlag
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import net.minestom.server.event.player.PlayerTickEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.time.Cooldown
import java.io.File

const val POWER_SYMBOL = "✘"
const val ATTACK_SYMBOL = "\uD83D\uDDE1"
const val MATTER_SYMBOL = "\uD83C\uDF0A"
const val MECHANICAL_PART_SYMBOL = "\uD83E\uDE93"
const val CLAIM_SYMBOL = "◎"
const val COLONY_SYMBOL = "⚐"
const val BUILDING_SYMBOL = "⧈"

val players = Json.decodeFromString<MutableMap<String, PlayerData>>(
    if (File("./player-data.json").exists())
        File("./player-data.json").readText()
    else "{}"
)
lateinit var instance: InstanceContainer private set

fun main() {
    // Initialize the server
    val minecraftServer = MinecraftServer.init()
    MojangAuth.init()



    instance = InstanceManager.createInstanceContainer().apply {

        setGenerator { unit ->
            unit.modifier().setAll { x, y, z ->
                if (x in 0..300 && y in 1 until 40 && z in 0..300) {
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
    GlobalEventHandler.listen<AsyncPlayerConfigurationEvent> { event ->
        event.spawningInstance = instance
        val player = event.player
        player.respawnPoint = Pos(5.0, 40.0, 5.0)
        player.gameMode = GameMode.ADVENTURE
        player.flyingSpeed = 0.5f
        player.isAllowFlying = true
        println(players[player.uuid.toString()] == null)
    }

    GlobalEventHandler.listen<PlayerSpawnEvent> { e ->
        if (players[e.player.uuid.toString()] == null) {
            SelectBlockItem.setAllSlots(e.player)
        } else {
            SelectBuildingItem.setItemSlot(e.player)
        }
    }

    SchedulerManager.scheduleTask({
        for (uuid in players.keys) {
            players[uuid]!!.tick(instance)
        }
    }, TaskSchedule.tick(30), TaskSchedule.tick(30))

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
        e.isCancelled = when(item) {
            SelectBlockItem.getItem(uuid) -> SelectBlockItem.onInteract(e, instance)
            ColonyItem.getItem(uuid) -> ColonyItem.onInteract(e, instance)
            ClaimItem.getItem(uuid) -> ClaimItem.onInteract(e, instance)
            SelectBuildingItem.getItem(uuid) -> SelectBuildingItem.onInteract(e, instance)
            TrainingCampItem.getItem(uuid) -> TrainingCampItem.onInteract(e, instance)
            MatterExtractorItem.getItem(uuid) -> MatterExtractorItem.onInteract(e, instance)
            MatterContainerItem.getItem(uuid) -> MatterContainerItem.onInteract(e, instance)
            BarracksItem.getItem(uuid) -> BarracksItem.onInteract(e, instance)

            else -> true
        }
    }

    GlobalEventHandler.listen<PlayerSwapItemEvent> {
        it.isCancelled = true
    }

    GlobalEventHandler.listen<PlayerTickEvent> { e ->
//        println("${players[e.player.uuid]?.power}")
        val playerData = players[e.player.uuid.toString()] ?: return@listen

        val target = e.player.getTargetBlockPosition(20)
        if (target != null) {
            when(val block = instance.getBlock(target)) {
                Block.GRASS_BLOCK -> {
                    var canAccess = false
                    for (x in (target.blockX() - 1)..(target.blockX() + 1)) {
                        for (z in (target.blockZ() - 1)..(target.blockZ() + 1)) {
                            if (x == target.blockX() && z == target.blockZ()) continue
                            if (instance.getBlock(Vec(x.toDouble(), target.blockY().toDouble(), z.toDouble())) == playerData.block) {
                                canAccess = true
                                break
                            }
                        }
                        if (canAccess) break
                    }
                    if (canAccess) {
                        e.player.inventory.claim(playerData.claimLevel, playerData.claimCost)
                    } else {
                        ColonyItem.setItemSlot(e.player)
                    }
                }
                else -> {
                    if (block == playerData.block) {
                        OwnedBlockItem.setItemSlot(e.player)
                    }
                }
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
    }

    // Start the server
    minecraftServer.start("0.0.0.0", 25565)
}

fun PlayerInventory.attack() {
    set(0, attackItem("", 10))
}

fun PlayerInventory.claim(claimLevel: Int, powerCost: Int) {
    set(0, claimItem(claimLevel, powerCost))
}

fun PlayerInventory.colony(powerCost: Int) {
    set(0, colonyItem(powerCost))
}

fun PlayerInventory.idle() {
    set(0, idleItem())
}

fun PlayerInventory.selectBlock() {
    for (i in 1..9)
        set(i, item(Material.STRUCTURE_VOID) {
            itemName = "<green><bold>$COLONY_SYMBOL Select Block $COLONY_SYMBOL".asMini()
        })
}

fun attackItem(target: String, powerCost: Int): ItemStack = item(Material.IRON_SWORD) {
    itemName = "<red>$ATTACK_SYMBOL <bold>Attack $target's Land</bold> <dark_gray>-<red> $POWER_SYMBOL $powerCost".asMini()
    amount = 1
}

fun claimItem(claimLevel: Int, powerCost: Int): ItemStack {
    val item = listOf(Material.WOODEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE)[claimLevel]
    return item(item) {
        itemName = "<gold>$CLAIM_SYMBOL <bold>Expand</bold> <dark_gray>-<red> $POWER_SYMBOL $powerCost".asMini()
        amount = 1
    }
}

fun idleItem(): ItemStack = item(Material.GRAY_DYE) {
    itemName = "".asMini()
    amount = 1
}

fun colonyItem(powerCost: Int): ItemStack = item(Material.CHEST) {
    itemName = "<green>$COLONY_SYMBOL Instantiate Colony <dark_gray>-<red> $POWER_SYMBOL $powerCost".asMini()
}