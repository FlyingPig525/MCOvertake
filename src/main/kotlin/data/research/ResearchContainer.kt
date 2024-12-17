package io.github.flyingpig525.data.research

import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.action.ActionData.*
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onAttacked
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onBuildWall
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onClaimLand
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onDestroyBuilding
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPlaceColony
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPlaceRaft
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPostAttack
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPreAttack
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onUpgradeWall
import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.Serializable

@Serializable
class ResearchContainer : Iterable<ResearchCurrency> {
    var unlockedCurrency = 1
    val basicResearch: BasicResearch = BasicResearch()

    fun currencyById(id: Int): ResearchCurrency? = when(id) {
        1 -> basicResearch
        else -> null
    }

    override fun iterator(): Iterator<ResearchCurrency> =
        iterator {
            yield(basicResearch)
        }

    fun onClaimLand(eventData: ClaimLand): ClaimLand {
        var data = basicResearch.upgrades.onClaimLand(eventData)
        return data
    }
    fun onPostAttack(eventData: PostAttack): PostAttack {
        var data = basicResearch.upgrades.onPostAttack(eventData)
        return data
    }
    fun onPreAttack(eventData: PreAttack): PreAttack {
        var data = basicResearch.upgrades.onPreAttack(eventData)
        return data
    }
    fun onAttacked(eventData: Attacked): Attacked {
        var data = basicResearch.upgrades.onAttacked(eventData)
        return data
    }
    fun onPlaceRaft(eventData: PlaceRaft): PlaceRaft {
        var data = basicResearch.upgrades.onPlaceRaft(eventData)
        return data
    }
    fun onPlaceColony(eventData: PlaceColony): PlaceColony {
        var data = basicResearch.upgrades.onPlaceColony(eventData)
        return data
    }
    fun onBuildWall(eventData: BuildWall): BuildWall {
        var data = basicResearch.upgrades.onBuildWall(eventData)
        return data
    }
    fun onUpgradeWall(eventData: UpgradeWall): UpgradeWall {
        var data = basicResearch.upgrades.onUpgradeWall(eventData)
        return data
    }
    fun onDestroyBuilding(eventData: DestroyBuilding): DestroyBuilding {
        var data = basicResearch.upgrades.onDestroyBuilding(eventData)
        return data
    }
}
