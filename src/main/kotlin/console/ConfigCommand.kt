package io.github.flyingpig525.console

import io.github.flyingpig525.config
import io.github.flyingpig525.data.Config
import io.github.flyingpig525.log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// TODO: Add config manipulation
object ConfigCommand : Command {
    init {
        Command.registry += ConfigCommand
    }

    override val arguments: Int = 1
    override val names: List<String> = listOf("config")

    override fun validate(arguments: List<String>): Boolean {
        return arguments.size == 1 && arguments[0] in names
    }

    override fun execute(arguments: List<String>) {
        log("Reading config file")
        val configFile = File("config.json")
        if (!configFile.exists()) {
            configFile.createNewFile()
            configFile.writeText(Json.encodeToString(Config()))
        }
        config = Json.decodeFromString<Config>(configFile.readText())
        log("Loaded config refreshed")
    }
}