package io.github.flyingpig525.console

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.flyingpig525.config
import io.github.flyingpig525.data.config.getCommentString
import io.github.flyingpig525.instances
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

object SaveCommand : Command {
    init {
        Command.registry += SaveCommand
    }

    override val arguments: Int = 1
    override val names: List<String> = listOf("save")

    override fun validate(arguments: List<String>): Boolean {
        return (arguments.size == 1 || arguments[1] == "config") && arguments[0] in names
    }

    override fun execute(arguments: List<String>) {
        if (arguments.size == 1) {
            log("Saving...")
            runBlocking { launch(Dispatchers.IO) {
                try {
                    instances.values.onEach {
                        it.save()
                        System.gc()
                    }
                } catch (e: Exception) {
                    log("An exception has occurred during game saving!", LogType.EXCEPTION)
                    log(e)
                }
            }}
            log("Game data saved")
        } else {
            log("Saving main config")
            val configFile = File("config.json5")
            if (!configFile.exists()) {
                configFile.createNewFile()
            }
            configFile.writeText(getCommentString(config))
            log("Saved main config")
        }
    }
}