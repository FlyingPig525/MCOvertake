package io.github.flyingpig525.data

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

object Sounds {
    val COLONY = sound("entity.player.levelup", 2f, 2f)
    val EXPAND = sound("block.gravel.break")
    val ATTACK = sound("item.axe.strip", 2f, 0.7f)
    val UPGRADE_WALL = sound("block.anvil.place", 2f, 2f)
    val PLACE_BUILDING = sound("block.anvil.use", 2f, 2f)
    val DESTROY_BUILDING = sound("block.anvil.break")
    val BUILD_RAFT = sound("block.bamboo_wood.place", 2f, 1.5f)

    val UNDERGROUND_ENTER = sound("block.sculk_sensor.clicking", 2f, 0.7f)
    val UNDERGROUND_LEAVE = sound("entity.player.teleport", 2f, 2f)

    val ERROR = sound("item.shield.block", 1f, 1.2f)

    private fun sound(key: String, volume: Float = 2f, pitch: Float = 1f): Sound {
        return Sound.sound(Key.key(key), Sound.Source.PLAYER, volume, pitch)
    }
}