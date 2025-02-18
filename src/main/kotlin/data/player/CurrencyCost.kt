package io.github.flyingpig525.data.player

import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

data class CurrencyCost(
    val organicMatter: Double = 0.0,
    val power: Double = 0.0,
    val mechanicalParts: Int = 0,
    val plastic: Int = 0,
    val lubricant: Int = 0
) {
    fun genericOrganicMatter(count: Int, cost: Double): CurrencyCost {
        val generalCost = (count * cost) + cost
        if (generalCost > 10000) {
            return copy(organicMatter = (generalCost/1000) * 1000)
        } else if (generalCost > 1000) {
            return copy(organicMatter = (generalCost/100) * 100)
        }
        return copy(organicMatter = generalCost)
    }
    fun genericPower(count: Int, cost: Double): CurrencyCost {
        val generalCost = (count * cost) + cost
        if (generalCost > 10000) {
            return copy(power = (generalCost/1000) * 1000)
        } else if (generalCost > 1000) {
            return copy(power = (generalCost/100) * 100)
        }
        return copy(power = generalCost)
    }
    fun genericMechanicalParts(count: Int, cost: Int): CurrencyCost {
        val generalCost = (count * cost) + cost
        if (generalCost > 10000) {
            return copy(mechanicalParts = (generalCost/1000) * 1000)
        } else if (generalCost > 1000) {
            return copy(mechanicalParts = (generalCost/100) * 100)
        }
        return copy(mechanicalParts = generalCost)
    }
    fun genericPlastic(count: Int, cost: Int): CurrencyCost {
        val generalCost = (count * cost) + cost
        if (generalCost > 10000) {
            return copy(plastic = (generalCost/1000) * 1000)
        } else if (generalCost > 1000) {
            return copy(plastic = (generalCost/100) * 100)
        }
        return copy(plastic = generalCost)
    }
    fun genericLubricant(count: Int, cost: Int): CurrencyCost {
        val generalCost = (count * cost) + cost
        if (generalCost > 10000) {
            return copy(lubricant = (generalCost/1000) * 1000)
        } else if (generalCost > 1000) {
            return copy(lubricant = (generalCost/100) * 100)
        }
        return copy(lubricant = generalCost)
    }

    fun apply(data: BlockData): ApplicationResult {
        var msg = Component.empty()
        var canAfford = true
        fun msg(c: Component) {
            if (canAfford) {
                msg = msg.append(c)
            } else {
                msg = msg.appendNewline().append(c) as TextComponent
            }
        }
        if (data.organicMatter < organicMatter) {
            msg("<red><bold>Not enough Organic Matter</bold> (${data.organicMatter}/$organicMatter)".asMini())
            canAfford = false
        }
        if (data.power < power) {
            msg("<red><bold>Not enough Power</bold> (${data.power}/$power)".asMini())
            canAfford = false
        }
        if (data.mechanicalParts < mechanicalParts) {
            msg("<red><bold>Not enough Mechanical Parts</bold> (${data.mechanicalParts}/$mechanicalParts)".asMini())
            canAfford = false
        }
        if (data.plastic < plastic) {
            msg("<red><bold>Not enough Plastic</bold> (${data.plastic}/$plastic)".asMini())
            canAfford = false
        }
        if (data.lubricant < lubricant) {
            msg("<red><bold>Not enough Lubricant</bold> (${data.lubricant}/$lubricant)".asMini())
            canAfford = false
        }
        if (!canAfford) return ApplicationResult.Fail(msg)
        data.organicMatter -= organicMatter
        data.power -= power
        data.mechanicalParts -= mechanicalParts
        data.plastic -= plastic
        data.lubricant -= lubricant
        return ApplicationResult.Success
    }

    companion object {
        fun genericOrganicMatter(count: Int, cost: Double): CurrencyCost = CurrencyCost().genericOrganicMatter(count, cost)
        fun genericPower(count: Int, cost: Double): CurrencyCost = CurrencyCost().genericPower(count, cost)
        fun genericMechanicalParts(count: Int, cost: Int): CurrencyCost = CurrencyCost().genericMechanicalParts(count, cost)
        fun genericPlastic(count: Int, cost: Int): CurrencyCost = CurrencyCost().genericPlastic(count, cost)
        fun genericLubricant(count: Int, cost: Int): CurrencyCost = CurrencyCost().genericLubricant(count, cost)
        val NONE = CurrencyCost(0.0, 0.0, 0, 0, 0)
    }

    sealed class ApplicationResult {
        data object Success : ApplicationResult()
        data class Fail(val message: Component) : ApplicationResult()
    }
}