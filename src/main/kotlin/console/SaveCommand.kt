package io.github.flyingpig525.console

import io.github.flyingpig525.instance
import io.github.flyingpig525.players
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object SaveCommand : Command {
    init {
        Command.registry += SaveCommand
    }

    override val arguments: Int = 1
    override val name: String = "save"

    override fun validate(arguments: List<String>): Boolean {
        return arguments.size == 1 && arguments[0] == name
    }

    override fun execute(arguments: List<String>) {
        val file = File("./player-data.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(Json.encodeToString(players))
        instance.saveChunksToStorage()
    }
}