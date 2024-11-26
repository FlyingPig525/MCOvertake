package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*

object BreakBuildingItem : Actionable {

    init {
        Actionable.registry += this
        Actionable.persistentRegistry += this
    }

    override val identifier: String = "building:destroy"


    override fun getItem(uuid: UUID): ItemStack {
        return item(Material.IRON_PICKAXE) {
            itemName = "<gold>$PICKAXE_SYMBOL <bold>Destroy Building</bold>".asMini()
            amount = 1
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()] ?: return true
        val target = event.player.getTrueTarget(20) ?: return true
        val buildingPos = target.withY(40.0)
        val buildingBlock = instance.getBlock(buildingPos)
        if (!Building.blockIsBuilding(buildingBlock)) return true
        val identifier = Building.getBuildingIdentifier(buildingBlock) ?: return true
        val ref = data.getBuildingReferenceByIdentifier(identifier) ?: return true
        ref.get().count--
        ref.get().select(event.player, data)
        instance.setBlock(buildingPos, Block.AIR)

        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[5] = getItem(player.uuid)
    }
}