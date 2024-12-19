package io.github.flyingpig525.console

import cz.lukynka.prettylog.log
import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.instances
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object SaveCommand : Command {
    init {
        Command.registry += SaveCommand
    }

    override val arguments: Int = 1
    override val names: List<String> = listOf("save")

    override fun validate(arguments: List<String>): Boolean {
        return arguments.size == 1 && arguments[0] in names
    }

    override fun execute(arguments: List<String>) {
        log("Saving...")
        instances.values.onEach { it.save() }
        log("Game data saved")
    }
}