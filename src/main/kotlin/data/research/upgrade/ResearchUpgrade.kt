package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.GameInstance
import io.github.flyingpig525.data.research.action.ActionData.*
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.ItemDsl
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.entity.Player
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@Serializable
// TODO: MAKE COST USE A DIFFERENT CLASS THAT ALLOWS MULTI-CURRENCY COSTS
/**
 * @property [name] - Unique (to currency) identifier
 */
sealed class ResearchUpgrade(open var level: Int = 0) {

    abstract var maxLevel: Int
    abstract val name: String
    abstract val cost: Long

    open fun onCurrencyUpgrade(newLevel: Int) {}
    /**
     * Executes after attacking another player
     */
    open fun onAttack(eventData: Attack): Attack? = null
    open fun onClaimLand(eventData: ClaimLand): ClaimLand? = null
    open fun onPlaceRaft(eventData: PlaceRaft): PlaceRaft? = null
    open fun onPlaceColony(eventData: PlaceColony): PlaceColony? = null
    open fun onBuildWall(eventData: BuildWall): BuildWall? = null
    open fun onUpgradeWall(eventData: UpgradeWall): UpgradeWall? = null
    open fun onDestroyBuilding(eventData: DestroyBuilding): DestroyBuilding? = null

    /**
     * Executes before attacking another player
     */
    open fun onAttackCostCalculation(eventData: AttackCostCalculation): AttackCostCalculation? = null

    /**
     * Executes before attacking another player from water
     */
    open fun onWaterAttackCostCalculation(eventData: WaterAttackCostCalculation): WaterAttackCostCalculation? = null

    /**
     * Executes when another player attacks
     */
    open fun onAttacked(eventData: Attacked): Attacked? = null
    open fun onMatterBuildingTick(eventData: MatterBuildingTick): MatterBuildingTick? = null

    abstract fun item(currency: ResearchCurrency, gameInstance: GameInstance): ItemStack

    open fun onPurchase(clickEvent: InventoryConditionResult, currency: ResearchCurrency, player: Player): PurchaseState {
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

        val success get() = this is Success
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
        fun List<ResearchUpgrade>.onPostAttack(eventData: Attack): Attack {
            var data = eventData
            forEach {
                data = it.onAttack(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onAttackCostCalculation(eventData: AttackCostCalculation): AttackCostCalculation {
            var data = eventData
            forEach {
                data = it.onAttackCostCalculation(data) ?: data
            }
            return data
        }
        // Implemented
        fun List<ResearchUpgrade>.onWaterAttackCostCalculation(eventData: WaterAttackCostCalculation): WaterAttackCostCalculation {
            var data = eventData
            forEach {
                data = it.onWaterAttackCostCalculation(data) ?: data
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

fun researchItem(material: Material, upgrade: ResearchUpgrade, block: @ItemDsl (ItemStack.Builder.() -> Unit)): ItemStack {
    val item = item(material) {
        itemName =
            "<gold><bold>${upgrade.name} </bold><gray>-<aqua><bold> Level: ${upgrade.level}/${upgrade.maxLevel}".asMini()
        block()
    }
    val itemLore = item.get(ItemComponent.LORE) ?: mutableListOf()
    return item.withLore(itemLore.apply { add("<gold>Cost: ${upgrade.cost}".asMini().noItalic()) })
}