package io.github.flyingpig525.console

interface Command {
    /**
     * Amount of command arguments, including name.
     */
    val arguments: Int
    val names: List<String>

    fun validate(arguments: List<String>): Boolean
    fun execute(arguments: List<String>)

    companion object {
        val registry: MutableSet<Command> = mutableSetOf()
    }
}