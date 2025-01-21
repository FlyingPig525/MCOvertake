package io.github.flyingpig525.command

import io.github.flyingpig525.GameInstance.Companion.gameInstance
import io.github.flyingpig525.data
import io.github.flyingpig525.item.SelectBlockItem
import io.github.flyingpig525.removeBossBars
import io.github.flyingpig525.toUUID
import net.bladehunt.kotstom.dsl.kommand.buildSyntax
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.bladehunt.kotstom.extension.adventure.asMini
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.mojang.MojangUtils

val coopCommand = kommand {
    name = "coop"

    subkommand {
        name = "invite"

        val playerArg = ArgumentEntity("player").apply {
            onlyPlayers(true)
            singleEntity(true)

        }

        buildSyntax(playerArg) {
            onlyPlayers()

            executor {
                val target = context.get(playerArg).findFirstPlayer(player.instance, player)
                if (target?.equals(player) == true) {
                    player.sendMessage("<red><bold>You cannot send a co-op invite to yourself! duh".asMini())
                    return@executor
                }
                val data = player.data
                val instance = player.gameInstance ?: return@executor
                val parentUUID = instance.uuidParents[player.uuid.toString()]
                if (data == null || parentUUID != player.uuid.toString()) {
                    player.sendMessage("<red><bold>You must be the owner of a block to invite a player!".asMini())
                    return@executor
                }
                if (target == null) {
                    player.sendMessage("<red><bold>Target player not found!".asMini())
                    return@executor
                }
                if (instance.outgoingCoopInvites[player.uuid] == null) {
                    instance.outgoingCoopInvites[player.uuid] = mutableListOf(target.uuid to target.username)
                } else {
                    instance.outgoingCoopInvites[player.uuid]!! += target.uuid to target.username
                }
                if (instance.incomingCoopInvites[target.uuid] == null) {
                    instance.incomingCoopInvites[target.uuid] = mutableListOf(player.uuid to player.username)
                } else {
                    instance.incomingCoopInvites[target.uuid]!! += player.uuid to player.username
                }
                player.sendMessage("<aqua><bold>Co-op invite successfully sent to <green>${target.username}<aqua>!".asMini())
                var targetMessage = "<aqua><bold>Incoming co-op invite from <green>${player.username}<aqua>!".asMini()
                targetMessage = targetMessage.append("\n<reset><aqua>To accept run <green>/coop accept ${player.username}<aqua>, or click ".asMini())
                targetMessage = targetMessage.append {
                    "<light_purple>[here]".asMini()
                        .clickEvent(ClickEvent.runCommand("/coop accept ${player.username}"))
                }
                target.sendMessage(targetMessage)
            }
        }
    }

    subkommand {
        name = "accept"

        val playerArg = ArgumentString("player").apply {
            setSuggestionCallback { sender, ctx, suggestion ->
                val player = sender as Player
                val instance = player.gameInstance ?: return@setSuggestionCallback
                for ((_, name) in instance.incomingCoopInvites[player.uuid] ?: mutableListOf()) {
                    val current = ctx.getRaw("player")
                    if (current in name) {
                        suggestion.addEntry(SuggestionEntry(name))
                    }
                }
                if (suggestion.entries.size == 0) {
                    suggestion.entries += instance.incomingCoopInvites[player.uuid]?.map { SuggestionEntry(it.second) } ?: emptyList()
                }
            }
        }

        buildSyntax(playerArg) {
            executor {
                val instance = player.gameInstance ?: return@executor
                val targetUsername = context.get(playerArg)
                val targetUUID = instance.incomingCoopInvites[player.uuid]?.first { it.second == targetUsername }
                if (targetUUID == null) {
                    player.sendMessage("<red><bold>No invite from $targetUsername!".asMini())
                    return@executor
                }
                if (instance.outgoingCoopInvites[targetUUID.first]?.contains(player.uuid to player.username) != true) {
                    player.sendMessage("<red><bold>$targetUsername no longer has you on their invite list".asMini())
                    instance.incomingCoopInvites[player.uuid]!! -= targetUUID
                    return@executor
                }
                instance.incomingCoopInvites[player.uuid]!! -= targetUUID
                instance.outgoingCoopInvites[targetUUID.first]!! -= player.uuid to player.username
                val data = player.data
                instance.uuidParents[player.uuid.toString()] = targetUUID.first.toString()
                instance.playerData.remove(player.uuid.toString())
                if (data != null) {
                    instance.clearBlock(data.block)
                    player.removeBossBars()
                }
                player.data!!.setupPlayer(player)
                player.sendMessage("<aqua><bold>Successfully joined $targetUsername's co-op!".asMini())
                player.instance.getPlayerByUuid(targetUUID.first)
                    ?.sendMessage("<aqua><bold>${player.username} has joined your co-op!".asMini())
            }
        }
    }

    subkommand {
        name = "kick"

        val playerName = ArgumentString("player").apply {
            setSuggestionCallback { sender, ctx, suggestion ->
                val player = sender as Player
                val instance = player.gameInstance ?: return@setSuggestionCallback
                val childUUIDs = instance.uuidParentsInverse[player.uuid.toString()]?.filter { it != player.uuid.toString() } ?: return@setSuggestionCallback
                for (uuid in childUUIDs) {
                    val name = MojangUtils.getUsername(uuid.toUUID())
                    val current = ctx.getRaw("player")
                    if (current in name) {
                        suggestion.addEntry(SuggestionEntry(name))
                    }
                }
                if (suggestion.entries.size == 0) {
                    suggestion.entries += childUUIDs.map { SuggestionEntry(MojangUtils.getUsername(it.toUUID())) }
                }
            }
        }
        buildSyntax(playerName) {
            executor {
                val targetName = context.get(playerName)
                val targetUUID = MojangUtils.getUUID(targetName)
                val gameInstance = player.gameInstance
                if (gameInstance == null) {
                    player.sendMessage("<red><bold>You must be in a game instance to run this command".asMini())
                    return@executor
                }
                val childUUIDs = gameInstance.uuidParentsInverse[player.uuid.toString()]?.filter { it != player.uuid.toString() }
                if (childUUIDs.isNullOrEmpty()) {
                    player.sendMessage("<red><bold>You must be the owner of a co-op to kick players".asMini())
                    return@executor
                }
                if (targetUUID.toString() !in childUUIDs) {
                    player.sendMessage("<red><bold>$targetName is not present in your co-op".asMini())
                    return@executor
                }
                player.sendMessage("<green><bold>$targetName will be kicked from your co-op in ${gameInstance.instanceConfig.coopKickWaitTime} minute(s) ".asMini().let {
                    it.append("<light_purple>[why ${gameInstance.instanceConfig.coopKickWaitTime} minute(s)]".asMini().hoverEvent { HoverEvent.showText(
                        ("<light_purple>If the delay is high, this is done to increase the risk of inviting someone to your co-op," +
                                " meaning you must be more careful with those you decide to trust." +
                                "\nThey will only be notified once they are kicked.").asMini()
                    ) as HoverEvent<Any>})
                })
                player.instance.scheduler().scheduleTask({
                    val targetPlayer = gameInstance.instance.getPlayerByUuid(targetUUID)
                    if (targetPlayer != null) {
                        targetPlayer.inventory.clear()
                        SelectBlockItem.setAllSlots(targetPlayer)
                        targetPlayer.sendMessage(
                            "<aqua><bold>You have been kicked from ${player.username}'s co-op, this was scheduled one hour ago".asMini()
                        )
                    }
                    gameInstance.uuidParents[targetUUID.toString()] = targetUUID.toString()
                    TaskSchedule.stop()
                }, TaskSchedule.minutes(gameInstance.instanceConfig.coopKickWaitTime))
            }
        }
    }
}