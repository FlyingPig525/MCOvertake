package io.github.flyingpig525.console

import cz.lukynka.prettylog.log
import io.github.flyingpig525.config
import io.github.flyingpig525.instances
import io.github.flyingpig525.lobbyInstance
import net.minestom.server.utils.mojang.MojangUtils

object OpCommand : Command {
    init {
        Command.registry += OpCommand
    }

    override val arguments: Int = 2
    override val names: List<String> = listOf("op")

    override fun validate(arguments: List<String>): Boolean =
        arguments[0] in names && arguments.size == 2

    override fun execute(arguments: List<String>) {
        if (arguments[1] in config.opUsernames) return
        config.opUsernames += arguments[1]
        config.opUUID += MojangUtils.getUUID(arguments[1]).toString()
        log("Player ${arguments[1]} has been made an operator")
        SaveCommand.execute(listOf("save", "config"))
        for (player in lobbyInstance.players) {
            if (player.username == arguments[1]) {
                player.permissionLevel = 4
                return
            }
        }
        for (instance in instances.values) {
            for (player in instance.instance.players) {
                if (player.username == arguments[1]) {
                    player.permissionLevel = 4
                    return
                }
            }
        }
    }
}