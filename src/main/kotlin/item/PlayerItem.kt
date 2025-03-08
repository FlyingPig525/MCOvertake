package io.github.flyingpig525.item

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data.player.BlockData.Companion.getDataByBlock
import io.github.flyingpig525.data.player.BlockData.Companion.toBlockSortedList
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.ksp.Item
import io.github.flyingpig525.playerPosition
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.set
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

@Item
object PlayerItem : Actionable {
    override val identifier: String = "block:idle"
    override val itemMaterial: Material = Material.PURPLE_DYE

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        val player = instance.instance.getPlayerByUuid(uuid) ?: return ERROR_ITEM
        val target = player.getTrueTarget(20) ?: return ERROR_ITEM
        val targetData = instance.blockData.getDataByBlock(instance.instance.getBlock(target.playerPosition)) ?: return ERROR_ITEM
        val placement = instance.blockData.toBlockSortedList().indexOf(targetData) + 1
        if (placement == 0) return ERROR_ITEM
        return item(itemMaterial) {
            val color = when(placement) {
                1 -> "<gold>"
                2 -> "<#c0c0c0>"
                3 -> "<#cd7f32>"
                else -> "<light_purple>"
            }
            itemName = "<light_purple>${targetData.playerDisplayName} <gray>- $color#$placement".asMini()
        }
    }

    override fun setItemSlot(player: Player) {
        if (itemCheck(player, this)) return
        player.inventory[0] = getItem(player.uuid, player.gameInstance ?: return)
    }
}