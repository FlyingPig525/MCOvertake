package io.github.flyingpig525.data

import io.github.flyingpig525.DASH_BANNER
import io.github.flyingpig525.serialization.BlockSerializer
import kotlinx.serialization.Serializable
import net.minestom.server.instance.block.Block

// These comments will eventually be used for wiki stuff I guess
@Serializable
data class Config(
    // Server address for game server and pack server, set to your servers public ip
    val serverAddress: String = "0.0.0.0",
    // Server port for game server, should always stay as 25565
    val serverPort: Int = 25565,
    // Server port for resource pack server, recommended to stay 25566
    val packServerPort: Int = 25566,
    // Path to the resource pack to be sent to players, ensure it ends with ".zip"
    val resourcePackPath: String = "res/pack.zip",
    // Path to the player permission file, ensure it ends with ".json"
    val permissionFilePath: String = "res/permissions.json",
    // List of names to be used for game instances
    // Each name must be unique, as each instance will have its own directory
    val instanceNames: MutableSet<String> = mutableSetOf("instance1"),
    // List of usernames to be on the whitelist
    // No whitelist if empty
    val whitelisted: Set<String> = emptySet(),
    // Message to show non-whitelisted players
    val notWhitelistedMessage: String = "<red><bold>Player not whitelisted\n</bold><grey>$DASH_BANNER\n<gold><bold>Please contact the server owner if you believe this is a mistake",
    // Operator usernames
    val opUsernames: MutableSet<String> = mutableSetOf(),
    // Operator UUIDs (autogenerated)
    val opUUID: MutableSet<String> = mutableSetOf(),
    // Whether to print a message on auto save
    val printSaveMessages: Boolean = false,
    // Delay in which the process check the console for commands in milliseconds (set >5000)
    val consolePollingDelay: Long = 5000,
    // Duration the target particles last in ticks
    val targetParticleDuration: Int = 15
)