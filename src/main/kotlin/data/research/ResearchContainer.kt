package io.github.flyingpig525.data.research

import io.github.flyingpig525.data.research.action.ActionData.*
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onAttacked
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onBuildWall
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onClaimLand
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onDestroyBuilding
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onMatterBuildingTick
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPlaceColony
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPlaceRaft
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPostAttack
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onAttackCostCalculation
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onUpgradeWall
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onWaterAttackCostCalculation
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
    fun onPostAttack(eventData: Attack): Attack {
        var data = basicResearch.upgrades.onPostAttack(eventData)
        return data
    }
    fun onAttackCostCalculation(eventData: AttackCostCalculation): AttackCostCalculation {
        var data = basicResearch.upgrades.onAttackCostCalculation(eventData)
        return data
    }
    fun onWaterAttackCostCalculation(eventData: WaterAttackCostCalculation): WaterAttackCostCalculation {
        var data = basicResearch.upgrades.onWaterAttackCostCalculation(eventData)
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
    fun onMatterBuildingTick(eventData: MatterBuildingTick): MatterBuildingTick {
        var data = basicResearch.upgrades.onMatterBuildingTick(eventData)
        return data
    }
}
