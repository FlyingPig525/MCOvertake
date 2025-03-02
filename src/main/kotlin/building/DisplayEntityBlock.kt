package io.github.flyingpig525.building

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import java.util.*

interface DisplayEntityBlock {
    fun checkShouldSpawn(point: Point, instance: Instance): Boolean
    fun spawn(point: Point, instance: Instance, uuid: UUID)
    fun remove(point: Point, instance: Instance, uuid: UUID)
}