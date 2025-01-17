package io.github.flyingpig525.console

import cz.lukynka.prettylog.log
import io.github.flyingpig525.config
import io.github.flyingpig525.data.config.Config
import io.github.flyingpig525.data.config.InstanceConfig
import io.github.flyingpig525.data.config.getCommentString
import io.github.flyingpig525.instances
import io.github.flyingpig525.json
import io.github.flyingpig525.log.MCOvertakeLogType
import io.github.flyingpig525.parentInstanceConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

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
        log("Reading config file", MCOvertakeLogType.FILESYSTEM)
        val configFile = File("config.json5")
        if (!configFile.exists()) {
            configFile.createNewFile()
            configFile.writeText(Json.encodeToString(Config()))
        }
        config = Json.decodeFromString<Config>(configFile.readText())
        log("Loaded config refreshed", MCOvertakeLogType.FILESYSTEM)
        log("Reading parent instance config", MCOvertakeLogType.FILESYSTEM)
        val parentCFile = File("parent-instance-config.json5")
        if (!parentCFile.exists()) {
            parentCFile.createNewFile()
            parentCFile.writeText(getCommentString(InstanceConfig()))
        }
        parentInstanceConfig = json.decodeFromString(parentCFile.readText())
        log("Loaded parent instance config refreshed", MCOvertakeLogType.FILESYSTEM)
        log("Reading instance configs", MCOvertakeLogType.FILESYSTEM)
        instances.onEach { it.value.refreshConfig() }
        log("Loaded instance configs refreshed", MCOvertakeLogType.FILESYSTEM)
    }
}