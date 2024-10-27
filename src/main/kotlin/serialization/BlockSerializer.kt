package io.github.flyingpig525.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minestom.server.instance.block.Block

object BlockSerializer : KSerializer<Block> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Block", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Block {
        val str = decoder.decodeString()
        return Block.fromNamespaceId(str)!!
    }

    override fun serialize(encoder: Encoder, value: Block) {
        encoder.encodeString(value.namespace().asString())
    }
}