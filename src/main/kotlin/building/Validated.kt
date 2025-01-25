package io.github.flyingpig525.building

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance

interface Validated {
    fun validate(instance: Instance, point: Point): Boolean
}