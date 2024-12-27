package io.github.flyingpig525.data.player.permission

import io.github.flyingpig525.config
import io.github.flyingpig525.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minestom.server.entity.Player
import java.io.File

@Serializable
class PermissionManager {
    private val permissions: MutableMap<String, MutableList<Permission>> = mutableMapOf()

    /**
     * @return Whether this player already had [p] permission
     */
    fun addPermission(player: Player, p: Permission): Boolean {
        val perms = permissions[player.uuid.toString()]
        if (perms == null) {
            permissions[player.uuid.toString()] = mutableListOf(p)
            jsonEncode()
            return false
        }
        if (p in perms) return true
        perms += p
        jsonEncode()
        return false
    }

    fun hasPermission(player: Player, p: Permission): Boolean {
        var perms: List<Permission> = permissions[player.uuid.toString()]?.toList() ?: return false
        for ((i, str) in p.permissions.withIndex()) {
            perms = perms.filter {
                if (it.permissions[i] == "*") {
                    return true
                }
                it.permissions[i] == str
            }
        }
        return perms.isNotEmpty()
    }

    fun jsonEncode() {
        val file = File(config.permissionFilePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(json.encodeToString(this))
    }
}