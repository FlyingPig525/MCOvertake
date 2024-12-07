package io.github.flyingpig525.console

import io.github.flyingpig525.config
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
    }
}