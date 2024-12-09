package io.github.flyingpig525.building

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance

interface DisplayEntityBlock {
    fun checkShouldSpawn(point: Point, instance: Instance): Boolean
    fun spawn(point: Point, instance: Instance)
}