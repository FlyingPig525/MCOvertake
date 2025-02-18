package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building.Companion.building
import io.github.flyingpig525.building.category.UndergroundCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.dsl.blockDisplay
import io.github.flyingpig525.ksp.BuildingCompanion
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.BlockDisplayMeta
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*
import kotlin.reflect.KProperty1

@Serializable
class RockMiner : Building {
    override var count: Int = 0
    override val resourceUse: Int get() = 4 * count
    override val cost get() = CurrencyCost.genericMechanicalParts(count, 40)

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        playerTarget.buildingPosition.repeatDirection { point, dir ->
            if (instance.getBlock(point).defaultState() == Block.DEEPSLATE) {
                instance.setBlock(playerTarget.buildingPosition, block.building(identifier).withProperties(mapOf(
                    "attached" to "false",
                    "powered" to "true",
                    "facing" to dir.opposite
                )))
                return@repeatDirection true
            }
            false
        }
        spawn(playerTarget.buildingPosition, instance, data.uuid.toUUID()!!)
        count++
    }

    override fun select(player: Player) {
        player.inventory[BUILDING_INVENTORY_SLOT] = getItem(cost, count)
    }

    override fun tick(data: BlockData) {
        data.organicMatter += 4 * count
    }

    @BuildingCompanion("first", UndergroundCategory::class)
    companion object RockMinerCompanion : Building.BuildingCompanion, DisplayEntityBlock, Validated {
        override val block: Block = Block.TRIPWIRE_HOOK
        override val identifier: String = "matter:rock_miner"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::rockMiners

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack = item(Material.TRIPWIRE_HOOK) {
            itemName = "<green>$MATTER_SYMBOL Rock Miner <gray>-</gray><white> $MECHANICAL_SYMBOL ${cost.mechanicalParts}".asMini()
            lore {
                +"<dark_gray>Breaks down adjacent rock walls".asMini()
                +"<dark_gray>to create organic matter".asMini()
                +"<gray>Generates 2 $organicMatter every 30 ticks".asMini().noItalic()
                resourcesConsumed(4, count)
                amountOwned(count)
            }
            set(Tag.String("identifier"), identifier)
        }

        override fun getItem(playerData: BlockData): ItemStack =
            getItem(playerData.buildings.rockMiners.cost, playerData.buildings.rockMiners.count)

        override fun getResourceUse(currentDisposableResources: Int, count: Int): Int = currentDisposableResources + 4

        override fun checkShouldSpawn(point: Point, instance: Instance): Boolean =
            instance.getBlock(point.buildingPosition).defaultState() == block
                    && !instance.getNearbyEntities(point.buildingPosition, 0.2).any {
                it is DisplayEntityBlock && (it.entityMeta as BlockDisplayMeta).blockStateId == Block.FLOWER_POT
            }

        override fun spawn(point: Point, instance: Instance, uuid: UUID) {
            blockDisplay {
                hasGravity = false
                block = Block.FLOWER_POT
                entity {
                    setTag(Tag.UUID("player"), uuid)
                }
            }.setInstance(instance, point)
        }

        override fun validate(instance: Instance, point: Point): Boolean = point.isUnderground && point.buildingPosition.repeatDirection { point, dir ->
            instance.getBlock(point).defaultState() == Block.DEEPSLATE
        }
    }
}