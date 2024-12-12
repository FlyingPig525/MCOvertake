package io.github.flyingpig525.data.research

import io.github.flyingpig525.data.research.ResearchUpgrade.Companion.onClaimLand
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.basic.BasicResearch
import kotlinx.serialization.Serializable

@Serializable
class ResearchContainer : Iterable<ResearchCurrency> {
    var unlockedCurrency = 1
    val basicResearch: BasicResearch = BasicResearch()

    fun onClaimLand(actionData: ActionData.ClaimLand): ActionData.ClaimLand {
        var data = basicResearch.upgrades.onClaimLand(actionData)
        return data
    }

    fun currencyById(id: Int): ResearchCurrency? = when(id) {
        1 -> basicResearch
        else -> null
    }

    override fun iterator(): Iterator<ResearchCurrency> =
        iterator {
            yield(basicResearch)
        }
}