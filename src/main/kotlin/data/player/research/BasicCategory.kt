package io.github.flyingpig525.data.player.research

import io.github.flyingpig525.building.BasicResearchGenerator
import io.github.flyingpig525.data.player.PlayerData
import kotlinx.serialization.Serializable

@Serializable
class BasicCategory {
    val basicResearchStations = BasicResearchGenerator()
    val basicResearchStationCost get() = PlayerData.genericBuildingCost(basicResearchStations.count, 100)
}