package io.github.flyingpig525.console

import io.github.flyingpig525.logStream
import java.io.File

object LogCommand : Command {
    init {
        Command.registry += LogCommand
    }

    override val arguments: Int = 1
    override val name: String = "savelog"

    override fun validate(arguments: List<String>): Boolean =
        arguments.size == 1 && arguments[0] == name

    override fun execute(arguments: List<String>) {
        val logFile = File("log.log")
        if (logFile.exists()) {
            logFile.delete()
        }
        logFile.createNewFile()
        logStream
    }
}