package io.github.flyingpig525.item

import io.github.flyingpig525.*
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.wall.blockIsWall
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
    override val itemMaterial: Material = Material.IRON_PICKAXE


    override fun getItem(uuid: UUID): ItemStack {
        return item(itemMaterial) {
            itemName = "<gold>$PICKAXE_SYMBOL <bold>Destroy Building</bold>".asMini()
            amount = 1
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent, instance: Instance): Boolean {
        val data = players[event.player.uuid.toString()] ?: return true
        val target = event.player.getTrueTarget(20) ?: return true
        val playerBlockPos = target.playerPosition
        val groundPos = target.withY(39.0)
        if (instance.getBlock(playerBlockPos).defaultState() != data.block) return true
        val buildingPos = target.buildingPosition
        val buildingBlock = instance.getBlock(buildingPos)
        if (buildingBlock.defaultState() == Block.LILY_PAD) return true
        val identifier = Building.getBuildingIdentifier(buildingBlock)
        if (identifier == null) {
            if (!blockIsWall(buildingBlock)) return true
        } else {
            val ref = data.getBuildingReferenceByIdentifier(identifier) ?: return true
            ref.get().count--
            ref.get().select(event.player, data)
        }
        if (instance.getBlock(groundPos) == Block.WATER) {
            instance.setBlock(buildingPos, Block.LILY_PAD)
        } else {
            instance.setBlock(buildingPos, Block.AIR)
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[5] = getItem(player.uuid)
    }
}