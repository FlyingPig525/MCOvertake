package io.github.flyingpig525.data.player

import io.github.flyingpig525.GameInstance

class DataResolver(val instance: GameInstance) {
    operator fun get(uuid: String) = instance.playerData[instance.uuidParents[uuid] ?: uuid]
    operator fun set(uuid: String, value: PlayerData) {
        instance.playerData[instance.uuidParents[uuid] ?: uuid] = value
    }
}