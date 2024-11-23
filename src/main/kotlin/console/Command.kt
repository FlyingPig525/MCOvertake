package io.github.flyingpig525.console

interface Command {
    /**
     * Amount of command arguments, including name.
     */
    val arguments: Int
    val name: String

    fun validate(arguments: List<String>): Boolean
    fun execute(arguments: List<String>): Unit

    companion object {
        val registry: MutableList<Command> = mutableListOf()
    }
}