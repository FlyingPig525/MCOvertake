package io.github.flyingpig525.item.template

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.building.Building
import io.github.flyingpig525.building.Validated
import io.github.flyingpig525.buildingPosition
import io.github.flyingpig525.data
import io.github.flyingpig525.data.Sounds
import io.github.flyingpig525.data.player.CurrencyCost
import io.github.flyingpig525.getTrueTarget
import io.github.flyingpig525.item.*
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

open class BuildingItem(
    private val building: Building.BuildingCompanion,
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
            event.player.playSound(Sounds.ERROR)
            event.player.sendMessage("<red><bold>Invalid building position".asMini())
            return true
        }
        val newResources = building.getResourceUse(playerData.disposableResourcesUsed)
        if (newResources != playerData.disposableResourcesUsed && newResources > playerData.maxDisposableResources) {
            event.player.playSound(Sounds.ERROR)
            event.player.sendMessage("<red><bold>Reached Disposable Resources cap".asMini())
            return true
        }
        val building = building.playerRef.get(playerData.buildings)
        val cost = building.cost
        when (val res = cost.apply(playerData)) {
            is CurrencyCost.ApplicationResult.Success -> {
                event.player.playSound(Sounds.PLACE_BUILDING)
                building.place(target, instance, playerData)
                building.select(event.player)
                playerData.updateBossBars()
            }

            is CurrencyCost.ApplicationResult.Fail -> {
                event.player.playSound(Sounds.ERROR)
                event.player.sendMessage(res.message)
            }
        }
        return true
    }

    override fun setItemSlot(player: Player) {
        building.playerRef.get(player.data?.buildings ?: return).select(player)
    }
}