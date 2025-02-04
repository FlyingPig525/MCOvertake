package io.github.flyingpig525.item.template

import com.sun.jdi.InvalidTypeException
import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.building.Validated
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data
import io.github.flyingpig525.data.player.BlockData
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.item.Actionable
import io.github.flyingpig525.item.ERROR_ITEM
import io.github.flyingpig525.item.checkBlockAvailable
import io.github.flyingpig525.item.sneakCheck
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*
import kotlin.reflect.KMutableProperty1

open class BuildingItem<T : Number>(
    private val building: Building.BuildingCompanion,
    private val currencyRef: KMutableProperty1<BlockData, T>,
    private val currencyName: String,
    override val itemMaterial: Material = building.block.registry().material()!!
) : Actionable {
    override val identifier: String = building.identifier

    override fun getItem(uuid: UUID, instance: GameInstance): ItemStack {
        return building.getItem(instance.dataResolver[uuid] ?: return ERROR_ITEM)
    }

    override fun onInteract(event: PlayerUseItemEvent): Boolean {
        if (sneakCheck(event)) return true
        val instance = event.instance
        val target = event.player.getTrueTarget(20) ?: return true
        val playerData = event.player.data ?: return true
        if (!checkBlockAvailable(playerData, target, instance)) return true
        if (building is Validated && !building.validate(event.instance, target.buildingPosition))  {
            event.player.sendMessage("<red><bold>Invalid building position".asMini())
            return true
        }
        if (building.getResourceUse(playerData.disposableResourcesUsed) > playerData.maxDisposableResources) {
            return true
        }
        val building = building.playerRef.get(playerData.buildings)
        val cost = building.cost
        val currency = currencyRef.get(playerData)
        if (currency is Int) {
            if (currency < cost) {
                event.player.sendMessage("<red><bold>Not enough $currencyName </bold>(${currency}/${cost})".asMini())
                return true
            }
            currencyRef.set(playerData, (currency - cost) as T)
        } else if (currency is Double) {
            if (currency < cost) {
                event.player.sendMessage("<red><bold>Not enough $currencyName </bold>(${currency}/${cost})".asMini())
                return true
            }
            currencyRef.set(playerData, (currency - cost) as T)
        } else throw InvalidTypeException("Currency must be type Int or Double")
        building.place(target, instance, playerData)
        building.select(event.player)
        playerData.updateBossBars()
        return true
    }

    override fun setItemSlot(player: Player) {
        building.playerRef.get(player.data?.buildings ?: return).select(player)
    }
}