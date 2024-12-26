package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.research.action.ActionData.*
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemStack

@Serializable
// TODO: MAKE COST USE A DIFFERENT CLASS THAT ALLOWS MULTI-CURRENCY COSTS
/**
 * @property [name] - Unique (to currency) identifier
 * @property [requiredInternalLevel] - The internal [ResearchCurrency.currencyLevel] required for purchase, upgrade will not be visible without
 * meeting requirement
 */
sealed class ResearchUpgrade {
    @Required abstract var level: Int

    abstract var maxLevel: Int
    abstract val name: String
    abstract val requiredInternalLevel: Int
    abstract val cost: Long

    /**
     * Executes after attacking another player
     */
    open fun onPostAttack(eventData: PostAttack): PostAttack? = null
    open fun onClaimLand(eventData: ClaimLand): ClaimLand? = null
    open fun onPlaceRaft(eventData: PlaceRaft): PlaceRaft? = null
    open fun onPlaceColony(eventData: PlaceColony): PlaceColony? = null
    open fun onBuildWall(eventData: BuildWall): BuildWall? = null
    open fun onUpgradeWall(eventData: UpgradeWall): UpgradeWall? = null
    open fun onDestroyBuilding(eventData: DestroyBuilding): DestroyBuilding? = null

    /**
     * Executes before attacking another player
     */
    open fun onPreAttack(eventData: PreAttack): PreAttack? = null

    /**
     * Executes when another player attacks
     */
    open fun onAttacked(eventData: Attacked): Attacked? = null
    open fun onMatterBuildingTick(eventData: MatterBuildingTick): MatterBuildingTick? = null

    abstract fun item(): ItemStack

    open fun onPurchase(clickEvent: InventoryConditionResult, currency: ResearchCurrency): PurchaseState {
        if (level == maxLevel) return PurchaseState.MaxLevel(currency, this)
        if (currency.count < cost) return PurchaseState.NotEnoughCurrency(currency, this)
        currency.count -= cost
        level++
        return PurchaseState.Success
    }

    sealed class PurchaseState {
        class NotEnoughCurrency(val currency: ResearchCurrency, val upgrade: ResearchUpgrade) : PurchaseState() {
            override fun toString(): String =
                "<red><bold>Not enough </bold><${currency.color}>${currency.symbol}<red><bold> to purchase! </bold>" +
                    "<gray>-<red> Missing ${currency.count}/${upgrade.cost}"
        }
        class MaxLevel(val currency: ResearchCurrency, val upgrade: ResearchUpgrade) : PurchaseState() {
            override fun toString(): String = "<red><bold>Max Upgrade Level ${upgrade.maxLevel} Reached!"
        }
        data object Success : PurchaseState()
    }

    companion object {
        // Implemented
        fun List<ResearchUpgrade>.onClaimLand(eventData: ClaimLand): ClaimLand {
            var data = eventData
            forEach {
                data = it.onClaimLand(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onPostAttack(eventData: PostAttack): PostAttack {
            var data = eventData
            forEach {
                data = it.onPostAttack(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onPreAttack(eventData: PreAttack): PreAttack {
            var data = eventData
            forEach {
                data = it.onPreAttack(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onAttacked(eventData: Attacked): Attacked {
            var data = eventData
            forEach {
                data = it.onAttacked(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onPlaceRaft(eventData: PlaceRaft): PlaceRaft {
            var data = eventData
            forEach {
                data = it.onPlaceRaft(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onPlaceColony(eventData: PlaceColony): PlaceColony {
            var data = eventData
            forEach {
                data = it.onPlaceColony(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onBuildWall(eventData: BuildWall): BuildWall {
            var data = eventData
            forEach {
                data = it.onBuildWall(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onUpgradeWall(eventData: UpgradeWall): UpgradeWall {
            var data = eventData
            forEach {
                data = it.onUpgradeWall(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onDestroyBuilding(eventData: DestroyBuilding): DestroyBuilding {
            var data = eventData
            forEach {
                data = it.onDestroyBuilding(data) ?: data
            }
            return data
        }
        fun List<ResearchUpgrade>.onMatterBuildingTick(eventData: MatterBuildingTick): MatterBuildingTick {
            var data = eventData
            forEach {
                data = it.onMatterBuildingTick(data) ?: data
            }
            return data
        }
    }
}