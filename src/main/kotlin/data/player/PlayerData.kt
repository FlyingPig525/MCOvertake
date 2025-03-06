package io.github.flyingpig525.data.player

import io.github.flyingpig525.GameInstance.Companion.gameInstance
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player

@Serializable
class PlayerData {
    @Transient
    val lastTeleporterPos: MutableSet<Point> = mutableSetOf()
    @Transient
    var targetWallLevel: Int = 0
    @Transient
    var bulkWallQueueFirstPos: Point? = null
    @Transient
    var bulkWallQueueFirstPosJustReset = false

    var playtime: Long = 0

    companion object {
        val Player.playerData: PlayerData? get() = gameInstance?.playerData?.get(uuid.toString())
    }
}