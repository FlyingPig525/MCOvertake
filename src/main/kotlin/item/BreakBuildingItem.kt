package io.github.flyingpig525.item

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.fromInstance
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.ksp.Item
import io.github.flyingpig525.wall.blockIsWall
import io.github.flyingpig525.wall.wallLevel
import net.bladehunt.kotstom.dsl.item.amount
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.util.*

@Item(persistent = true)
object BreakBuildingItem : Actionable {

    override val identifier: String = "building:destroy"
    override val itemMaterial: Material = Material.IRON_PICKAXE


    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return item(itemMaterial) {
            itemName = "<gold>$PICKAXE_SYMBOL <bold>Destroy Building</bold>".asMini()
            amount = 1
            set(Tag.String("identifier"), identifier)
        }
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        val instance = event.instance
        val data = event.player.data ?: return true
        val target = event.player.getTrueTarget(20) ?: return true
        val playerBlockPos = target.playerPosition
        val groundPos = target.visiblePosition
        if (instance.getBlock(playerBlockPos).defaultState() != data.block) return true
        val buildingPos = target.buildingPosition
        val buildingBlock = instance.getBlock(buildingPos)
        if (buildingBlock.defaultState() == Block.LILY_PAD) return true
        val identifier = Building.getBuildingIdentifier(buildingBlock)
        if (identifier == null) {
            if (!blockIsWall(buildingBlock)) return true
        } else {
            val ref = Building.getBuildingByIdentifier(identifier)?.playerRef?.get(data) ?: return true
            ref.count--
            ref.select(event.player, data)
            ActionData.DestroyBuilding(data, instance, event.player).apply {
                building = ref
                wallLevel = buildingBlock.wallLevel
            }.also { data.research.onDestroyBuilding(it) }
        }
        if (instance.getBlock(groundPos) == Block.WATER) {
            instance.setBlock(buildingPos, Block.LILY_PAD)
        } else {
            instance.setBlock(buildingPos, Block.AIR)
            buildingPos.repeatAdjacent {
                UpgradeWallItem.updateWall(it, instance)
            }
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        player.inventory[5] = getItem(player.uuid, instances.fromInstance(player.instance) ?: return)
    }
}