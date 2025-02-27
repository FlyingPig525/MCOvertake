package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.organicMatter
import io.github.flyingpig525.data.player.BlockData
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
        if (event.player.isSneaking && data.targetWallLevel != 0) {
            if (data.bulkWallQueueFirstPosJustReset) {
                data.bulkWallQueueFirstPosJustReset = false
                return true
            }
            if (data.bulkWallQueueFirstPos != null) {
                val lowX = min(data.bulkWallQueueFirstPos!!.x, target.x).toInt()
                val lowZ = min(data.bulkWallQueueFirstPos!!.z, target.z).toInt()
                val maxX = max(data.bulkWallQueueFirstPos!!.x, target.x).toInt()
                val maxZ = max(data.bulkWallQueueFirstPos!!.z, target.z).toInt()
                instance.getNearbyEntities(data.bulkWallQueueFirstPos!!, 0.2).onEach {
                    if (it.hasTag(Tag.Boolean("bulkWallUpgradeSelector"))) it.remove()
                }
                data.bulkWallQueueFirstPos = null
                data.bulkWallQueueFirstPosJustReset = true
                for (x in lowX..maxX) for (z in lowZ..maxZ) {
                    val pos = target.withX(x.toDouble()).withZ(z.toDouble())
                    if (instance.getBlock(pos).wallLevel != 0 && instance.getBlock(pos.playerPosition) == data.block) {
                        data.wallUpgradeQueue += pos to data.targetWallLevel
                        blockDisplay {
                            this.block = Block.LIME_STAINED_GLASS
                            hasGravity = false
                            scale = Vec(1.0, PIXEL_SIZE, 1.0)
                            entity {
                                setTag(Tag.Boolean("wallUpgrade"), true)
                            }
                        }.setInstance(event.instance, pos.buildingPosition)
                    }
                }
                return true
            }
            instance.scheduleNextTick {
                data.bulkWallQueueFirstPos = target
            }
            instance.scheduler().scheduleTask({
                if (data.bulkWallQueueFirstPos == null) return@scheduleTask TaskSchedule.stop()
                val player = instance.getPlayerByUuid(event.player.uuid) ?: return@scheduleTask  TaskSchedule.stop()
                val target = player.getTrueTarget(20)?.buildingPosition ?: return@scheduleTask TaskSchedule.tick(1)
                val lowX = min(data.bulkWallQueueFirstPos!!.x, target.x)
                val lowZ = min(data.bulkWallQueueFirstPos!!.z, target.z)
                val maxX = max(data.bulkWallQueueFirstPos!!.x, target.x) + 1
                val maxZ = max(data.bulkWallQueueFirstPos!!.z, target.z) + 1
                val y40 = target.withX(lowX).withZ(lowZ).add(0.0, PIXEL_SIZE, 0.0)
                val oneZero = y40.withX(maxX)
                val oneOne = oneZero.withZ(maxZ)
                val zeroOne = y40.withZ(maxZ)
                val targetParticles = mutableListOf<SendablePacket>()
                val color = Color(NamedTextColor.WHITE)
                val trailParticle = Particle.TRAIL.withColor(color).withDuration(config.targetParticleDuration)
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(oneZero), y40, Vec.ZERO, 1f, 10
                )
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(oneOne), oneZero, Vec.ZERO, 1f, 10
                )
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(zeroOne), oneOne, Vec.ZERO, 1f, 10
                )
                targetParticles += ParticlePacket(
                    trailParticle.withTarget(y40), zeroOne, Vec.ZERO, 1f, 10
                )
                player.sendPackets(targetParticles)
                TaskSchedule.tick(1)
            }, TaskSchedule.nextTick())
            return true
        }
        if (!data.wallUpgradeCooldown.isReady(Instant.now().toEpochMilli())) return true
        val block = instance.getBlock(target).defaultState()
        if (!block.canUpgradeWall) return true
        upgradeWall(block, target, data, instance, event.player)
        return true
    }

    fun upgradeWall(block: Block, position: Point, data: BlockData, instance: Instance, player: Player? = null): Boolean {
        val cost = getWallUpgradeCost(block) ?: return false
        val actionData = ActionData.UpgradeWall(data, instance).apply {
            this.cost = cost
            this.cooldown = Cooldown(Duration.ofSeconds(1))
        }.also { data.research.onUpgradeWall(it) }
        if (data.organicMatter < actionData.cost) {
            player?.sendMessage("<red><bold>Not enough Organic Matter</bold> (${data.organicMatter}/$${actionData.cost})".asMini())
            return false
        }
        data.organicMatter -= actionData.cost
        data.wallUpgradeCooldown = actionData.cooldown
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

    override fun onHandAnimation(event: PlayerHandAnimationEvent) {
        val data = event.player.data ?: return
        if (data.handAnimationWasDrop) return
        val target = event.player.getTrueTarget(20) ?: return
        val block = event.instance.getBlock(target.buildingPosition)
        val level = block.wallLevel
        if (event.player.isSneaking) {
            val blockMaterial = block.registry().material()!!
            val selectionInventory = Inventory(InventoryType.CHEST_3_ROW, "Wall Upgrade Planner")
            selectionInventory[4, 0] = item(blockMaterial) {
                itemName = "<green>Current Level: <gold><bold>$level".asMini()
                lore {
                    +"<dark_gray>${blockMaterial.name().replace("minecraft:", "")}".asMini().noItalic()
                }
            }
            val values = listOf(10, 5, 2, 1)
            for (i in 0..3) {
                selectionInventory[i, 1] = item(Material.RED_STAINED_GLASS_PANE) {
                    itemName = "<red>-${values[i]}".asMini()
                    setTag(Tag.Integer("value"), -values[i])
                }
                selectionInventory[8-i, 1] = item(Material.GREEN_STAINED_GLASS_PANE) {
                    itemName = "<green>+${values[i]}".asMini()
                    setTag(Tag.Integer("value"), values[i])
                }
            }
            selectionInventory[4, 1] = item(blockMaterial) {
                itemName = "<green>Target Level: <gold><bold>$level".asMini()
                lore {
                    +"<dark_gray>${blockMaterial.name().replace("minecraft:", "")}".asMini().noItalic()
                }
            }
            val confirmItem = item(Material.IRON_AXE) {
                itemName = "<green><bold>Confirm".asMini()
            }
            selectionInventory[4, 2] = confirmItem
            var targetLevel = level
            selectionInventory.addInventoryCondition { player, slot, type, res ->
                res.isCancel = true
                if (slot <= 17) {
                    val value = res.clickedItem.getTag(Tag.Integer("value")) ?: return@addInventoryCondition
                    targetLevel += value
                    targetLevel = targetLevel.coerceIn(1, maxWallLevel)
                    val targetMaterial = wall(targetLevel).registry().material()!!
                    (player.openInventory!! as Inventory)[4, 1] = item(targetMaterial) {
                        var cost = 0
                        for (i in 0..targetLevel) {
                            cost += getWallUpgradeCost(i)
                        }
                        itemName = "<green>Target Level: <gold><bold>$targetLevel <gray>- <green> $cost $organicMatter".asMini()
                        lore {
                            +"<dark_gray>${targetMaterial.name().replace("minecraft:", "")}".asMini().noItalic()
                        }
                    }
                } else if (res.clickedItem == confirmItem) {
                    data.targetWallLevel = targetLevel
                    player.closeInventory()
                }
            }
            event.player.openInventory(selectionInventory)
        } else if (target.buildingPosition in data.wallUpgradeQueue.map { it.first }) {
            data.wallUpgradeQueue.removeIf { it.first == target.buildingPosition }
            event.instance.getNearbyEntities(target.buildingPosition, 0.2).onEach {
                if (it.hasTag(Tag.Boolean("wallUpgrade"))) it.remove()
            }
        } else if (data.targetWallLevel != 0) {
            data.wallUpgradeQueue.addFirst(target.buildingPosition to data.targetWallLevel)
            blockDisplay {
                this.block = Block.LIME_STAINED_GLASS
                hasGravity = false
                scale = Vec(1.0, PIXEL_SIZE, 1.0)
                entity {
                    setTag(Tag.Boolean("wallUpgrade"), true)
                }
            }.setInstance(event.instance, target.buildingPosition)
        }
    }

    override fun setItemSlot(player: Player) {
        val gameInstance = instances.fromInstance(player.instance) ?: return
        player.inventory[0] = getItem(player.uuid, gameInstance)
    }
}