package io.github.flyingpig525.building

import net.minestom.server.coordinate.Point
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent

interface Interactable {

    /**
     * @return Whether to call [PlayerUseItemEvent]
     */
    fun onInteract(e: PlayerBlockInteractEvent): Boolean = true

    /**
     * @return Whether to call the player item's hand animation event
     */
    fun onHandAnimation(e: PlayerHandAnimationEvent, pos: Point): Boolean = true
}