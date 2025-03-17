package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.category.SkyCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.dsl.blockDisplay
import io.github.flyingpig525.entity.ToxicologyLabGasEntity
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*
import kotlin.reflect.KProperty1

@Serializable
class ToxicologyLab : Building() {
    override val resourceUse: Int
        get() = 6 * count
    override val cost: CurrencyCost
        get() = CurrencyCost
            .genericMechanicalParts(count, 1500)
            .genericPlastic(count, 250)
            .genericOrganicMatter(count, 900.0)
    override val itemGetter: (cost: CurrencyCost, count: Int) -> ItemStack
        get() = ::getItem

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        spawn(playerTarget.buildingPosition, instance, data.uuid.toUUID()!!)
        count++
    }

    override fun tick(data: BlockData) {
        if (count == 0) return
        val pollution = data.buildings.sumOf { if (it.producesPollution) it.count else 0 }.coerceAtMost(250)
        data.lubricant -= 20 * count
        data.power += 0.05 * pollution
    }

    @io.github.flyingpig525.ksp.BuildingCompanion(orderAfter = "PollutionExtractor", category = SkyCategory::class)
    companion object ToxicologyLabCompanion : BuildingCompanion, Validated, DisplayEntityBlock {
        // this is the first block display only building!
        override val block: Block = INVISIBLE_BLOCK
        override val identifier: String = "power:generator_3"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::toxicologyLabs

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.CONDUIT) {
                itemName = ("<red>$POWER_SYMBOL Toxicology Lab <gray>-</gray>" +
                        "<white> $MECHANICAL_SYMBOL ${cost.mechanicalParts} $plasticColor$PLASTIC_SYMBOL ${cost.plastic}" +
                        "<green> $MATTER_SYMBOL ${cost.organicMatter}").asMini()
                lore {
                    +"<dark_gray>Utilizes the carbon from pollution and".asMini()
                    +"<dark_gray>turns it into a concentrated toxic gas".asMini()
                    +"<dark_gray>Can only be placed in the sky".asMini()
                    +"<gray>Consumes 20 $lubricant per research tick".asMini().noItalic()
                    +"<gray>Produces 0.05 $power per pollution-producing building every 30 ticks".asMini().noItalic()
                    resourcesConsumed(6, count)
                    amountOwned(count)
                }
                set(Tag.String("identifier"), identifier)
            }
        }

        override fun getResourceUse(currentDisposableResources: Int, count: Int): Int = currentDisposableResources + 6

        override fun validate(instance: Instance, point: Point): Boolean {
            return point.isSky
        }

        override fun checkShouldSpawn(point: Point, instance: Instance): Boolean {
            val block = instance.getBlock(point.buildingPosition).defaultState() == block.defaultState()
            val entities = !instance.getNearbyEntities(point.buildingPosition, 0.2).any {
                it.getTag(Tag.String("identifier")) == identifier
            }
            return block && entities
        }

        override fun spawn(point: Point, instance: Instance, uuid: UUID) {
            blockDisplay {
                hasGravity = false
                block = Block.TINTED_GLASS
                scale = Vec(1.0 - (PIXEL_SIZE * 2), 1.0 - (PIXEL_SIZE * 2), 1.0 - (PIXEL_SIZE * 2))
                translation = Vec(PIXEL_SIZE, PIXEL_SIZE * 2, PIXEL_SIZE)
                entity {
                    setTag(Tag.UUID("player"), uuid)
                    setTag(Tag.String("identifier"), identifier)
                }
            }.setInstance(instance, point)

            blockDisplay {
                hasGravity = false
                block = Block.OBSIDIAN
                scale = Vec(1.0 - (PIXEL_SIZE * 2), PIXEL_SIZE * 2, 1.0 - (PIXEL_SIZE * 2))
                translation = Vec(PIXEL_SIZE, 0.0, PIXEL_SIZE)
                entity {
                    setTag(Tag.UUID("player"), uuid)
                    setTag(Tag.String("identifier"), identifier)
                }
            }.setInstance(instance, point)
            for (i in 0..2) {
                ToxicologyLabGasEntity(1.0 + ((-10..10).random() / 100.0)).apply {
                    setTag(Tag.UUID("player"), uuid)
                    setTag(Tag.String("identifier"), identifier)
                }.setInstance(instance, point)
            }
        }

        override fun remove(point: Point, instance: Instance, uuid: UUID) {
            instance.getNearbyEntities(point.buildingPosition, 0.2).filter {
                it.getTag(Tag.String("identifier")) == identifier
            }.onEach { it.remove() }
        }
    }

}