package io.github.flyingpig525.command

import cz.lukynka.prettylog.log
import io.github.flyingpig525.*
import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data.config.getCommentString
import io.github.flyingpig525.data.player.permission.Permission
import kotlinx.coroutines.Dispatchers
import net.bladehunt.kotstom.InstanceManager
import net.bladehunt.kotstom.command.Kommand
import net.bladehunt.kotstom.dsl.kommand.buildSyntax
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentBoolean
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.command.builder.arguments.number.ArgumentLong
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import java.io.File
import java.nio.file.Path

val lobbyCommand = Kommand("lobby", "hub", "s", "home").apply {
    defaultExecutor = CommandExecutor { sender, context ->
        if (sender !is Player) return@CommandExecutor
        if (sender.instance == lobbyInstance) return@CommandExecutor
        sender.sendMessage("<gray>Sending you to the <red>lobby<gray>...".asMini())
        sender.closeInventory()
        sender.inventory.clear()
        sender.instance = lobbyInstance
        sender.removeBossBars()
    }
}
val createInstanceCommand = kommand {
    name = "createInstance"
    buildSyntax {
        condition {
            permissionManager.hasPermission(player, Permission("instance.creation"))
        }
        executor {
            player.sendMessage("<red><bold>Missing required arguments!".asMini())
        }
    }
    val nameArg = ArgumentString("name")
    val researchArg = ArgumentBoolean("research")
    val skyIslandsArg = ArgumentBoolean("sky_islands")
    val mapSizeArg = ArgumentInteger("map_size").min(1).max(1000).setDefaultValue(300)
    val noOpArg = ArgumentBoolean("no_op_research")

    buildSyntax(nameArg, researchArg, skyIslandsArg, mapSizeArg, noOpArg) {
        condition {
            permissionManager.hasPermission(player, Permission("instance.creation"))
        }
        onlyPlayers()
        executorAsync(Dispatchers.IO) {
            val name = context.get(nameArg)
            val research = context.get(researchArg)
            val skyIslands = context.get(skyIslandsArg)
            val mapSize = context.get(mapSizeArg)
            val noOp = context.get(noOpArg)
            try {
                player.sendMessage("<green>Attempting to create instance $name".asMini())
                if (name == "") {
                    player.sendMessage("<red><bold>Invalid instance name!".asMini())
                    return@executorAsync
                }
                if (name in instances.keys) {
                    player.sendMessage("<red><bold>Instance \"$name\" already exists!".asMini())
                    return@executorAsync
                }
                instances[name] = GameInstance(
                    Path.of("instances", name),
                    name,
                    parentInstanceConfig.copy(
                        noiseSeed = (Long.MIN_VALUE..Long.MAX_VALUE).random(),
                        allowResearch = research,
                        generateSkyIslands = skyIslands,
                        mapSize = mapSize
                    )).apply {
                    totalInit(player)
                    this.noOp = noOp
                }
                config.instanceNames += name
                File("config.json5").writeText(getCommentString(config))
                player.sendMessage("<green><bold>Created instance \"$name\" successfully!".asMini())
            } catch(e: Exception) {
                // IllegalPathException doesnt exist but it does??????
                if (e::class.simpleName == "IllegalPathException") {
                    player.sendMessage("<red><bold>Name \"$name\" contains illegal characters!".asMini())
                }
                player.sendMessage("<red><bold>Something went wrong! </bold>(${e::class.simpleName} || ${e.message})".asMini())
                log(e)
            }
        }
    }
}
val deleteInstanceCommand = kommand {
    name = "removeInstance"
    val argument = ArgumentString("name").apply {
        setSuggestionCallback { sender, context, suggestion ->
            for (name in instances.keys) {
                val current = context.getRaw("name")
                if (current in name) {
                    suggestion.addEntry(SuggestionEntry(name))
                }
            }
            if (suggestion.entries.size == 0) {
                suggestion.entries += instances.keys.map { SuggestionEntry(it) }
            }
        }
    }

    buildSyntax {
        condition {
            permissionManager.hasPermission(player, Permission("instance.deletion"))
        }
        executor {
            player.sendMessage("<red><bold>Missing required arguments!".asMini())
        }
    }

    buildSyntax(argument) {
        condition {
            permissionManager.hasPermission(player, Permission("instance.deletion"))
        }
        onlyPlayers()
        executorAsync {
            val name = get(argument)
            if (name !in instances.keys) {
                player.sendMessage("<red><bold>Instance \"$name\" does not exist!".asMini())
                return@executorAsync
            }
            val gameInstance = instances[name]!!
            if (gameInstance.instance.players.isNotEmpty()) {
                player.sendMessage("<red><bold>Cannot delete an instance with players!".asMini())
                return@executorAsync
            }
            InstanceManager.unregisterInstance(gameInstance.instance)
            gameInstance.delete()
            instances.remove(name)
            config.instanceNames.remove(name)
            File("config.json5").writeText(getCommentString(config))
            player.sendMessage("<green><bold>Instance \"$name\" successfully removed!".asMini())
            System.gc()
        }
    }
}
val tpCommand = kommand {
    name = "tp"
    val targetArg = ArgumentEntity("player").apply {
        onlyPlayers(true)
        singleEntity(true)
    }

    buildSyntax(targetArg) {
        condition {
            permissionManager.hasPermission(player, Permission("instance.tp"))
        }
        executor {
            val target = context.get(targetArg)
            val targetPlayer = target.findFirstPlayer(sender)
            if (targetPlayer == null) {
                player.sendMessage("<red><bold>Player ${context.getRaw(targetArg)} does not exist")
                return@executor
            }
            player.teleport(targetPlayer.position)
        }
    }
}
val setGrass = kommand {
    name = "setGrass"

    val loc = ArgumentRelativeVec3("pos")
    buildSyntax(loc) {
        condition {
            permissionManager.hasPermission(player, Permission("instance.set"))
        }

        executor {
            val loc = context.get(loc).fromSender(sender)
            player.instance.setBlock(loc.playerPosition, Block.GRASS_BLOCK)
            player.instance.setBlock(loc.visiblePosition, Block.GRASS_BLOCK)
        }
    }
}
val setAllCommand = kommand {
    name = "setAll"

    val block = ArgumentBlockState("block")
    buildSyntax(block) {
        condition {
            permissionManager.hasPermission(player, Permission("instance.set"))
        }

        executorAsync {

            val block = context.get(block)
            val instanceConfig = player.gameInstance!!.instanceConfig
            val instance = player.instance
            for (x in 0..instanceConfig.mapSize) {
                for (z in 0..instanceConfig.mapSize) {
                    instance.loadChunk(Vec(x.toDouble(), z.toDouble())).thenRun {
                        onAllBuildingPositions(Vec(x.toDouble(), 1.0, z.toDouble())) {
                            val visible = instance.getBlock(it.visiblePosition).defaultState()
                            if (visible != Block.WATER && visible != Block.AIR) {
                                instance.setBlock(it.visiblePosition, block)
                                instance.setBlock(it.playerPosition, block)
                            }
                        }
                    }
                }
            }
        }
    }
}
val setTimeCommand = kommand {
    name = "setTime"
    val tick = ArgumentLong("tick")

    buildSyntax {
        condition {
            permissionManager.hasPermission(player, Permission("instance.time"))
        }
        executor {  }
    }

    buildSyntax(tick) {
        condition {
            permissionManager.hasPermission(player, Permission("instance.time"))
        }
        executor {
            val tick = context.get(tick)
            player.instance.time = tick
        }
    }
}