package io.github.flyingpig525.building

import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerUseItemEvent

interface Interactable {

    /**
     * @return Whether to call [PlayerUseItemEvent]
     */
    fun onInteract(e: PlayerBlockInteractEvent): Boolean
}