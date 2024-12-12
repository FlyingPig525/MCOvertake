package io.github.flyingpig525.data.research

import io.github.flyingpig525.cancel
import io.github.flyingpig525.data.research.ResearchUpgrade.Companion.onPreAttack
import io.github.flyingpig525.data.research.action.*
import io.github.flyingpig525.data.research.action.ActionData.*
import io.github.flyingpig525.players
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.Component
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.item.ItemStack

@Serializable
// TODO: MAKE COST USE A DIFFERENT CLASS THAT ALLOWS MULTI-CURRENCY COSTS
/**
 * @param [name] - Unique (to currency) identifier
 * @param [requiredInternalLevel] - The internal [ResearchCurrency.currencyLevel] required for purchase, upgrade will not be visible without
 * meeting requirement
 * @param [parent] - The parent [ResearchCurrency]
 */
open class ResearchUpgrade(var name: String, private val requiredInternalLevel: Int) {
    open val item: ItemStack get() = ItemStack.AIR
    open val cost: Int get() = 0
    var level: Int = 0
    var maxLevel: Int = 5

    open fun onPostAttack(eventData: PostAttack): PostAttack? = null
    open fun onClaimLand(eventData: ClaimLand): ClaimLand? = null
    open fun onPlaceRaft(eventData: PlaceRaft): PlaceRaft? = null
    open fun onPlaceColony(eventData: PlaceColony): PlaceColony? = null
    open fun onBuildWall(eventData: BuildWall): BuildWall? = null
    open fun onPreAttack(eventData: PreAttack): PreAttack? = null
    open fun onAttacked(eventData: Attacked): Attacked? = null

    open fun onPurchase(clickEvent: InventoryClickEvent, currency: ResearchCurrency): Boolean {
        if (level == maxLevel) return false
        if (currency.count < cost) return false
        currency.count -= cost
        level++
        return true
    }

    companion object {
        fun List<ResearchUpgrade>.onClaimLand(eventData: ClaimLand): ClaimLand {
            var data = eventData
            forEach {
                data = it.onClaimLand(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onPostAttack(eventData: PostAttack): PostAttack {
            var data = eventData
            forEach {
                data = it.onPostAttack(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onPreAttack(eventData: PreAttack): PreAttack {
            var data = eventData
            forEach {
                data = it.onPreAttack(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onAttacked(eventData: Attacked): Attacked {
            var data = eventData
            forEach {
                data = it.onAttacked(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onPlaceRaft(eventData: PlaceRaft): PlaceRaft {
            var data = eventData
            forEach {
                data = it.onPlaceRaft(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onPlaceColony(eventData: PlaceColony): PlaceColony {
            var data = eventData
            forEach {
                data = it.onPlaceColony(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onBuildWall(eventData: BuildWall): BuildWall {
            var data = eventData
            forEach {
                data = it.onBuildWall(data) ?: data
            }
            return data
        }
    }
}