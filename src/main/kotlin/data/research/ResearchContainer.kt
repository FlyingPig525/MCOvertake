package io.github.flyingpig525.data.research

import io.github.flyingpig525.data.research.action.ActionData.*
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.HiddenResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onAttackCostCalculation
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onAttacked
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onBuildWall
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onClaimLand
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onDestroyBuilding
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onMatterBuildingTick
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPlaceColony
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPlaceRaft
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onPostAttack
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onUpgradeWall
import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade.Companion.onWaterAttackCostCalculation
import kotlinx.serialization.Serializable

@Serializable
class ResearchContainer : Iterable<ResearchCurrency> {
    var unlockedCurrency = 1
    val basicResearch: BasicResearch = BasicResearch()
    val hiddenResearch = HiddenResearch()

    fun currencyById(id: Int): ResearchCurrency? = when(id) {
        1 -> basicResearch
        else -> null
    }

    override fun iterator(): Iterator<ResearchCurrency> =
        iterator {
            yield(hiddenResearch)
            yield(basicResearch)
        }

    fun onClaimLand(eventData: ClaimLand): ClaimLand {
        var data = eventData
        forEach {
            data = it.onClaimLand(data)
        }
        return data
    }
    fun onPostAttack(eventData: Attack): Attack {
        var data = eventData
        forEach {
            data = it.onPostAttack(data)
        }
        return data
    }
    fun onAttackCostCalculation(eventData: AttackCostCalculation): AttackCostCalculation {
        var data = eventData
        forEach {
            data = it.onAttackCostCalculation(data)
        }
        return data
    }
    fun onWaterAttackCostCalculation(eventData: WaterAttackCostCalculation): WaterAttackCostCalculation {
        var data = eventData
        forEach {
            data = it.onWaterAttackCostCalculation(data)
        }
        return data
    }
    fun onAttacked(eventData: Attacked): Attacked {
        var data = eventData
        forEach {
            data = it.onAttacked(data)
        }
        return data
    }
    fun onPlaceRaft(eventData: PlaceRaft): PlaceRaft {
        var data = eventData
        forEach {
            data = it.onPlaceRaft(data)
        }
        return data
    }
    fun onPlaceColony(eventData: PlaceColony): PlaceColony {
        var data = eventData
        forEach {
            data = it.onPlaceColony(data)
        }
        return data
    }
    fun onBuildWall(eventData: BuildWall): BuildWall {
        var data = eventData
        forEach {
            data = it.onBuildWall(data)
        }
        return data
    }
    fun onUpgradeWall(eventData: UpgradeWall): UpgradeWall {
        var data = eventData
        forEach {
            data = it.onUpgradeWall(data)
        }
        return data
    }
    fun onDestroyBuilding(eventData: DestroyBuilding): DestroyBuilding {
        var data = eventData
        forEach {
            data = it.onDestroyBuilding(data)
        }
        return data
    }
    fun onMatterBuildingTick(eventData: MatterBuildingTick): MatterBuildingTick {
        var data = eventData
        forEach {
            data = it.onMatterBuildingTick(data)
        }
        return data
    }
}
