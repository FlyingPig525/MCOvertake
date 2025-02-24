@file:Suppress("unused")

package io.github.flyingpig525.dsl

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.BlockDisplayMeta
import net.minestom.server.instance.block.Block

class BlockDisplayDsl {
    val entity = Entity(EntityType.BLOCK_DISPLAY)
    var block: Block
        set(value) {
            (entity.entityMeta as BlockDisplayMeta).setBlockState(value)
        }
        get() = (entity.entityMeta as BlockDisplayMeta).blockStateId
    var hasGravity: Boolean
        set(value) {
            entity.setNoGravity(!value)
        }
        get() = !entity.hasNoGravity()
    var scale: Vec
        set(value) {
            (entity.entityMeta as BlockDisplayMeta).scale = value
        }
        get() = (entity.entityMeta as BlockDisplayMeta).scale
    var translation: Point
        set(value) {
            (entity.entityMeta as BlockDisplayMeta).translation = value
        }
        get() = (entity.entityMeta as BlockDisplayMeta).translation
    var glowing: Boolean
        set(value) {
            (entity.entityMeta as BlockDisplayMeta).isHasGlowingEffect = value
        }
        get() = (entity.entityMeta as BlockDisplayMeta).isHasGlowingEffect
    var glowColor: Int
        set(value) {
            (entity.entityMeta as BlockDisplayMeta).glowColorOverride = value
        }
        get() = (entity.entityMeta as BlockDisplayMeta).glowColorOverride
    var customName: Component?
        set(value) {
            (entity.entityMeta as BlockDisplayMeta).customName = value
        }
        get() = (entity.entityMeta as BlockDisplayMeta).customName
    var width: Double
        set(value) {
            scale = scale.withX(value)
        }
        get() = scale.x
    var length: Double
        set(value) {
            scale = scale.withZ(value)
        }
        get() = scale.z
    var height: Double
        set(value) {
            scale = scale.withY(value)
        }
        get() = scale.y
    fun meta(block: BlockDisplayMeta.() -> Unit) {
        (entity.entityMeta as BlockDisplayMeta).block()
    }
    fun entity(block: Entity.() -> Unit) {
        entity.block()
    }
}

inline fun blockDisplay(block: BlockDisplayDsl.() -> Unit): Entity {
    return BlockDisplayDsl().apply(block).entity
}