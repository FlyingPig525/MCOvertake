package io.github.flyingpig525.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec

object PointSerializer : KSerializer<Point> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("Point", DoubleArraySerializer().descriptor)

    override fun deserialize(decoder: Decoder): Point {
        val collection = decoder.decodeSerializableValue(DoubleArraySerializer())
        return Vec(collection[0], collection[1], collection[2])
    }

    override fun serialize(encoder: Encoder, value: Point) {
        encoder.encodeSerializableValue(DoubleArraySerializer(), doubleArrayOf(value.x(), value.y(), value.z()))
    }
}