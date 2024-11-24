package io.github.flyingpig525.console

import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

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
    }
}