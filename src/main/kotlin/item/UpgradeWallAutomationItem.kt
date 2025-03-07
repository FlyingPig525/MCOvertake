package io.github.flyingpig525.item

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.building.organicMatter
import io.github.flyingpig525.data.player.PlayerData.Companion.playerData
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
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.tag.Tag
import net.minestom.server.timer.TaskSchedule
import java.lang.Exception
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Item
object UpgradeWallAutomationItem : Actionable {
    override val identifier: String = "building:upgrade_wall_automation"
    override val itemMaterial: Material = Material.GOLDEN_AXE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<gold>$WALL_SYMBOL <bold>Configure Auto-Upgrading</bold>".asMini()
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun setItemSlot(player: Player) {
        player.inventory[1] = getItem(player.uuid, player.gameInstance ?: return)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data = event.player.data ?: return true
        val playerData = event.player.playerData ?: return true
        val target = event.player.getTrueTarget(20)?.buildingPosition ?: return true
        if (playerData.targetWallLevel == 0) {
            event.player.sendMessage("<red>Target wall level is not set".asMini())
            return true
        }
        if (playerData.bulkWallQueueFirstPosJustReset) {
            playerData.bulkWallQueueFirstPosJustReset = false
            return true
        }
        if (playerData.bulkWallQueueFirstPos != null) {
            val lowX = min(playerData.bulkWallQueueFirstPos!!.x, target.x).toInt()
            val lowZ = min(playerData.bulkWallQueueFirstPos!!.z, target.z).toInt()
            val maxX = max(playerData.bulkWallQueueFirstPos!!.x, target.x).toInt()
            val maxZ = max(playerData.bulkWallQueueFirstPos!!.z, target.z).toInt()
            instance.getNearbyEntities(playerData.bulkWallQueueFirstPos!!, 0.2).onEach {
                if (it.hasTag(Tag.Boolean("bulkWallUpgradeSelector"))) it.remove()
            }
            playerData.bulkWallQueueFirstPos = null
            playerData.bulkWallQueueFirstPosJustReset = true
            for (x in lowX..maxX) for (z in lowZ..maxZ) {
                val pos = target.withX(x.toDouble()).withZ(z.toDouble())
                if (instance.getBlock(pos).wallLevel != 0 && instance.getBlock(pos.playerPosition) == data.block) {
                    data.wallUpgradeQueue += pos to playerData.targetWallLevel
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
            playerData.bulkWallQueueFirstPos = target
        }
        instance.scheduler().scheduleTask({
            try {
                if (playerData.bulkWallQueueFirstPos == null) return@scheduleTask TaskSchedule.stop()
                val player = instance.getPlayerByUuid(event.player.uuid) ?: return@scheduleTask TaskSchedule.stop()
                val target = player.getTrueTarget(20)?.buildingPosition ?: return@scheduleTask TaskSchedule.tick(1)
                val lowX = min(playerData.bulkWallQueueFirstPos!!.x, target.x)
                val lowZ = min(playerData.bulkWallQueueFirstPos!!.z, target.z)
                val maxX = max(playerData.bulkWallQueueFirstPos!!.x, target.x) + 1
                val maxZ = max(playerData.bulkWallQueueFirstPos!!.z, target.z) + 1
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
            } catch (e: Exception) {
                log("An exception occurred during the mass selection task!", LogType.EXCEPTION)
                log(e)
            }
            TaskSchedule.tick(1)
        }, TaskSchedule.nextTick())
        return true
    }

    override fun onHandAnimation(event: PlayerHandAnimationEvent) {
        val data = event.player.data ?: return
        val playerData = event.player.playerData ?: return
        if (data.handAnimationWasDrop) return
        val target = event.player.getTrueTarget(20) ?: return
        val block = event.instance.getBlock(target.buildingPosition)
        val level = block.wallLevel
        if (!event.player.isSneaking) {
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
                selectionInventory[8 - i, 1] = item(Material.GREEN_STAINED_GLASS_PANE) {
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
                        itemName =
                            "<green>Target Level: <gold><bold>$targetLevel <gray>- <green> $cost $organicMatter".asMini()
                        lore {
                            +"<dark_gray>${targetMaterial.name().replace("minecraft:", "")}".asMini().noItalic()
                        }
                    }
                } else if (res.clickedItem == confirmItem) {
                    playerData.targetWallLevel = targetLevel
                    player.closeInventory()
                }
            }
            event.player.openInventory(selectionInventory)
        } else if (target.buildingPosition in data.wallUpgradeQueue.map { it.first }) {
            data.wallUpgradeQueue.removeIf { it.first == target.buildingPosition }
            event.instance.getNearbyEntities(target.buildingPosition, 0.2).onEach {
                if (it.hasTag(Tag.Boolean("wallUpgrade"))) it.remove()
            }
        } else if (playerData.targetWallLevel != 0) {
            data.wallUpgradeQueue.addFirst(target.buildingPosition to playerData.targetWallLevel)
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
}