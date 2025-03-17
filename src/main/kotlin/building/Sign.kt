package io.github.flyingpig525.building

import io.github.flyingpig525.*
import io.github.flyingpig525.building.category.BasicCategory
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.ksp.PlayerBuildings
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket
import net.minestom.server.tag.Tag
import javax.swing.text.html.HTML
import kotlin.reflect.KProperty1

@Serializable
class Sign : Building(), Interactable {
    override val resourceUse: Int get() = 0
    override val cost: CurrencyCost get() = CurrencyCost.genericOrganicMatter(0, 100.0)
    override val itemGetter: (cost: CurrencyCost, count: Int) -> ItemStack
        get() = ::getItem

    override fun place(playerTarget: Point, instance: Instance, data: BlockData) {
        instance.setBlock(playerTarget.buildingPosition, block.building(identifier))
        count++
    }

    override fun onInteract(e: PlayerBlockInteractEvent): Boolean {
        if (!e.player.isSneaking) return true
        val data = e.player.data ?: return true
        if (e.instance.getBlock(e.blockPosition.playerPosition) != data.block) return true
        e.player.sendPacket(OpenSignEditorPacket(e.blockPosition, true))
        return false
    }

    override fun onHandAnimation(e: PlayerHandAnimationEvent, pos: Point): Boolean {
        val data = e.player.data ?: return true
        val player = e.instance.getBlock(pos.playerPosition)
        val block = e.instance.getBlock(pos)
        if (player != data.block) return true
        var rotation = ((block.getProperty("rotation")?.toInt() ?: 0) + 1)
        if (rotation > 14) rotation = 0
        e.instance.setBlock(pos, block.withProperty("rotation", "$rotation"))
        return false
    }

    @io.github.flyingpig525.ksp.BuildingCompanion("UndergroundTeleporter", BasicCategory::class)
    companion object SignCompanion : BuildingCompanion {
        override val block: Block = Block.OAK_SIGN
        override val identifier: String = "sign"
        override val playerRef: KProperty1<PlayerBuildings, Building> = PlayerBuildings::signs

        override fun getItem(cost: CurrencyCost, count: Int): ItemStack {
            return item(Material.OAK_SIGN) {
                itemName = "<#d8b589>Sign <gray>- <green>$MATTER_SYMBOL 100".asMini()

                lore {
                    +"<dark_gray>Allows you to place a unique mark on the land".asMini()
                    resourcesConsumed(0, count)
                    amountOwned(count)
                }
                setTag(Tag.String("identifier"), identifier)
            }
        }

        override fun getItem(playerData: BlockData): ItemStack =
            getItem(playerData.buildings.signs.cost, playerData.buildings.signs.count)

        override fun getResourceUse(currentDisposableResources: Int, count: Int): Int = currentDisposableResources


    }
}