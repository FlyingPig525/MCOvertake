package io.github.flyingpig525.data.player.permission

import kotlinx.serialization.Serializable

@Serializable
data class Permission(val id: String) {
    val permissions: List<String> = id.split('.')
}