package io.github.flyingpig525.data

import io.github.flyingpig525.serialization.BlockSerializer
import kotlinx.serialization.Serializable
import net.minestom.server.instance.block.Block

@Serializable
data class InstanceConfig(
    // Scale for generation noise
    val noiseScale: Double = 0.03,
    // Threshold for noise result to be considered grass, else water
    val noiseThreshold: Double = 0.35,
    // Seed for noise, randomly generated when config is created. Change when resetting map
    val noiseSeed: Long = (Long.MIN_VALUE..Long.MAX_VALUE).random(),
    // Length and width of map (will always be a square)
    val mapSize: Int = 300,
    // Block identifier for underground claimable tile, can be any block, but it is recommended to keep it a block players cant be
    @Serializable(with = BlockSerializer::class) val undergroundBlock: Block = Block.BEDROCK,
)