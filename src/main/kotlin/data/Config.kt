package io.github.flyingpig525.data

import io.github.flyingpig525.DASH_BANNER
import jdk.jfr.Threshold
import kotlinx.serialization.Serializable
@Serializable
data class Config(
    val serverAddress: String = "0.0.0.0",
    val serverPort: Int = 25565,
    val whitelisted: List<String> = emptyList(),
    val notWhitelistedMessage: String = "<red><bold>Player not whitelisted\n</bold><grey>$DASH_BANNER\n<gold><bold>Please contact the server owner if you believe this is a mistake",
    val printSaveMessages: Boolean = false,
    val consolePollingDelay: Long = 2500,
    val doNoiseTest: Boolean = false,
    val noiseScale: Double = 0.05,
    val noiseThreshold: Double = 0.15
)