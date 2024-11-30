package io.github.flyingpig525.console

import io.github.flyingpig525.data.PlayerData
import io.github.flyingpig525.instance
import io.github.flyingpig525.log
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
        return arguments.size in 1..2 && arguments[0] == name
    }

    override fun execute(arguments: List<String>) {
        if (arguments.size == 1) {
            log("Saving...")
            val file = File("./player-data.json")
            if (!file.exists()) {
                file.createNewFile()
            }
            file.writeText(Json.encodeToString(players))
            instance.saveChunksToStorage()
            log("Game data saved")
        } else if (arguments[1] == "reload") {
            log("Reading save...")
            players = Json.decodeFromString<MutableMap<String, PlayerData>>(
                if (File("./player-data.json").exists())
                    File("./player-data.json").readText()
                else "{}"
            )
            log("Save loaded")
        }
    }
}