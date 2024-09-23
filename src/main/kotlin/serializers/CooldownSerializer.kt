package io.github.flyingpig525.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minestom.server.utils.time.Cooldown
import java.time.Duration

object CooldownSerializer : KSerializer<Cooldown> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Cooldown", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Cooldown {
        return Cooldown(Duration.ofMillis(decoder.decodeLong()))
    }

    override fun serialize(encoder: Encoder, value: Cooldown) {
        encoder.encodeLong(value.duration.toMillis())
    }

}