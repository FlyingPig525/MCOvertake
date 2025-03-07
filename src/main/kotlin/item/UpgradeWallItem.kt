package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.organicMatter
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.PlayerData.Companion.playerData
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.dsl.blockDisplay
import io.github.flyingpig525.ksp.Item
import io.github.flyingpig525.wall.*
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.bladehunt.kotstom.extension.x
import net.bladehunt.kotstom.extension.z
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.particle.Particle
import net.minestom.server.tag.Tag
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.time.Cooldown
import java.lang.Exception
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Item
object UpgradeWallItem : Actionable {
    override val identifier: String = "building:upgrade_wall"
    override val itemMaterial: Material = Material.IRON_AXE


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val target = instance.instance.getPlayerByUuid(uuid)!!.getTrueTarget(20) ?: return ERROR_ITEM
        val block = instance.instance.getBlock(target.buildingPosition)
        val upgradeCost = getWallUpgradeCost(block) ?: return ERROR_ITEM
        return item(itemMaterial) {
            itemName = "<gold>$WALL_SYMBOL <bold>Upgrade Wall</bold><dark_grey> - <green>$MATTER_SYMBOL $upgradeCost".asMini()
            if (!block.canUpgradeWall) {
                itemName = "<gold>$WALL_SYMBOL <bold>Max Level".asMini()
            }
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data = event.player.data ?: return true
        val target = event.player.getTrueTarget(20)?.buildingPosition ?: return true
        if (!data.wallUpgradeCooldown.isReady(Instant.now().toEpochMilli())) return true
        val block = instance.getBlock(target).defaultState()
        if (!block.canUpgradeWall) return true
        upgradeWall(block, target, data, instance, event.player)
        return true
    }

    fun upgradeWall(block: Block, position: Point, data: BlockData, instance: Instance, player: Player? = null): Boolean {
        val cost = getWallUpgradeCost(block) ?: return false
        var cooldownMs = 600L
        position.playerPosition.repeatAdjacent {
            val block = instance.getBlock(it).defaultState()
            if (block != Block.GRASS_BLOCK && block != Block.SAND && block != data.block && block != Block.AIR && block != Block.DIAMOND_BLOCK) {
                cooldownMs = 1400L
            }
        }
        val actionData = ActionData.UpgradeWall(data, instance).apply {
            this.cost = cost
            this.cooldown = Cooldown(Duration.ofMillis(cooldownMs))
        }.also { data.research.onUpgradeWall(it) }
        if (data.organicMatter < actionData.cost) {
            player?.sendMessage("<red><bold>Not enough Organic Matter</bold> (${data.organicMatter}/${actionData.cost})".asMini())
            return false
        }
        data.organicMatter -= actionData.cost
        if (data.getPlayers().isEmpty()) {
            data.wallUpgradeCooldown = Cooldown(actionData.cooldown.duration.multipliedBy(4L))
        } else {
            data.wallUpgradeCooldown = Cooldown(actionData.cooldown.duration.multipliedBy(2L))
        }
        data.sendPacket(
            SetCooldownPacket(
                itemMaterial.cooldownIdentifier,
                data.wallUpgradeCooldown.ticks
            )
        )
        instance.setBlock(position, nextWall(block.wallLevel))
        updateWall(position, instance)
        position.repeatAdjacent { updateWall(it, instance) }
        return true
    }

    fun updateWall(point: Point, instance: Instance) {
        val block = instance.getBlock(point)
        val level = block.wallLevel
        when(level) {
            1 -> { WallItem.updateIronBar(point, instance) }

            in WOODEN_FENCE_RANGE -> {
                updateWallDirections(instance, point, WOODEN_FENCE_RANGE)
            }

            in BRICK_FENCE_RANGE -> {
                updateWallDirections(instance, point, BRICK_FENCE_RANGE)
            }

            in WALL_RANGE -> {
                updateWallDirections(instance, point, WALL_RANGE)
            }

            in GLASS_PANE_RANGE -> {
                updateWallDirections(instance, point, GLASS_PANE_RANGE)
            }
        }
    }

    private fun updateWallDirections(instance: Instance, point: Point, range: IntRange, block: Block = instance.getBlock(point).defaultState()) {
        val north = instance.getBlock(point.add(0.0, 0.0, -1.0)).defaultState().wallLevel in range
        val south = instance.getBlock(point.add(0.0, 0.0, 1.0)).defaultState().wallLevel in range
        val east = instance.getBlock(point.add(1.0, 0.0, 0.0)).defaultState().wallLevel in range
        val west = instance.getBlock(point.add(-1.0, 0.0, 0.0)).defaultState().wallLevel in range

        val block = block.withProperties(mapOf(
            "north" to if (range == WALL_RANGE) if (north) "low" else "none" else "$north",
            "south" to if (range == WALL_RANGE) if (south) "low" else "none" else "$south",
            "east" to if (range == WALL_RANGE) if (east) "low" else "none" else "$east",
            "west" to if (range == WALL_RANGE) if (west) "low" else "none" else "$west"
        ))
        instance.setBlock(point, block)
    }



    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[0] = getItem(player.uuid, gameInstance)
    }
}