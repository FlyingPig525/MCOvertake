package io.github.flyingpig525.data.player

import io.github.flyingpig525.GameInstance
import java.util.UUID

class DataResolver(val instance: GameInstance) {
    operator fun get(uuid: String) = instance.blockData[instance.uuidParents[uuid] ?: uuid]
    operator fun set(uuid: String, value: PlayerData) {
        instance.blockData[instance.uuidParents[uuid] ?: uuid] = value
    }
    operator fun get(uuid: UUID) = get(uuid.toString())
    operator fun set(uuid: UUID, value: PlayerData) = set(uuid.toString(), value)
}