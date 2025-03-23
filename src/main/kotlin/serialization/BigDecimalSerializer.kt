package io.github.flyingpig525.serialization

import kotlinx.serialization.*
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.math.MathContext

object BigDecimalSerializer : KSerializer<BigDecimal> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("BigDecimal", DoubleArraySerializer().descriptor)


    override fun deserialize(decoder: Decoder): BigDecimal {
        val collection = decoder.decodeSerializableValue(DoubleArraySerializer())
        return BigDecimal(collection[0], MathContext(collection[1].toInt()))
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeSerializableValue(DoubleArraySerializer(), doubleArrayOf(value.toDouble(), value.precision().toDouble()))
    }
}