package io.github.flyingpig525.entity

import de.articdive.jnoise.core.util.MathUtil
import info.laht.threekt.math.*
import io.github.flyingpig525.dsl.blockDisplay
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.extension.editMeta
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.BlockDisplayMeta
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.utils.MathUtils
import java.math.MathContext
import kotlin.math.sin

class ToxicologyLabGasEntity(private val startingScale: Double = 1.1) : Entity(EntityType.ITEM_DISPLAY) {
    private val euler = Euler(degToRad(45), (0..10).random().toFloat(), (0..10).random().toFloat())
    private val rotation = Quaternion().setFromEuler(euler)
    private var x = startingScale
    private var z = startingScale
    private var y = startingScale
    private val vec get() = Vec(x, y, z)
    init {
        editMeta<ItemDisplayMeta> {
            itemStack = item(Material.GREEN_STAINED_GLASS)
            displayContext = ItemDisplayMeta.DisplayContext.FIXED
            translation = Vec(0.5, 0.5, 0.5)
            scale = vec
            leftRotation = rotation.toArray()
            posRotInterpolationDuration = 1
            transformationInterpolationDuration = 1
        }
//            block = Block.LIME_STAINED_GLASS
//            scale = Vec(0.4, 0.4, 0.4)
//            hasGravity = false
//            meta {
//                leftRotation = rotation.toArray()
//                posRotInterpolationDuration = 5
//                transformationInterpolationDuration = 5
//            }
//        }
    }

    override fun tick(time: Long) {
        super.tick(time)
        editMeta<ItemDisplayMeta> {
            euler.z += degToRad(1)
            euler.y += degToRad(1)
            x = startingScale + (0.08 * sin((time.toDouble() + (startingScale * 45)) / 4))
            y = startingScale + (0.08 * sin((time.toDouble() + (startingScale * 45)) / 4))
            z = startingScale + (0.08 * sin((time.toDouble() + (startingScale * 45)) / 4))
            scale = vec
            rotation.setFromEuler(euler)
            leftRotation = rotation.toArray()
//            println(leftRotation.map { it })
        }
    }
}