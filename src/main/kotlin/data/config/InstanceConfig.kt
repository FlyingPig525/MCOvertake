package io.github.flyingpig525.data.config

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
    // Whether to allow research upgrades or not
    val allowResearch: Boolean = false,
    // The amount of time a co-op kicked is queued for in minutes
    // Can be lowered to make inviting the wrong people less punishing, but that is not recommended
    val coopKickWaitTime: Long = 60,
    // Whether sky islands should be generated
    val generateSkyIslands: Boolean = false
) {
    init {
        assert(noiseScale in 0.0..2.0) { "Noise scale must be within 0 and 2" }
        assert(noiseThreshold in 0.0..1.0) { "Noise threshold must be within 0 and 1" }
        assert(noiseSeed in Long.MIN_VALUE..Long.MAX_VALUE) { "Noise seed must be within ${Long.MIN_VALUE} and ${Long.MAX_VALUE}" }
        assert(mapSize > 0) { "Map size must be greater than 0" }
        assert(coopKickWaitTime >= 0) { "Coop kick wait time must be greater than or equal to 0" }
    }
    companion object : CommentContainer {
        override val comments: List<String> = listOf(
            "// Scale for generation noise. Default: 0.03",
            "// Threshold for noise result to be considered grass, else water. Default: 0.35",
            "// Seed for noise, randomly generated when config is created. Change when resetting map. No default",
            "// Length and width of map (will always be a square). Default: 300",
            "// Block identifier for underground claimable tile.\n\t" +
                    "// Can be any block, but it is recommended to keep it a block players cant be for visual purposes. Default: \"minecraft:bedrock\"",
            "// Whether to allow research upgrades or not",
            "// The amount of time a co-op kicked is queued for in minutes\n\t" +
                    "// Can be lowered to make inviting the wrong people less punishing, but that is not recommended. Default: 60",
            "// Whether sky islands should be generated. Default: false"
        )
    }
}