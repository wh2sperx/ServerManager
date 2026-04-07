package dev.wh2sperx.manager

import dev.wh2sperx.ErrorType
import dev.wh2sperx.manager.model.IsValid
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PermissionManager {
    lateinit var api: LuckPerms private set
    private val groupName: String = "wh2sperx"

    fun initialize() {
        val prod = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        if (prod != null) {
            api = prod.provider
            createGroupIfNotExist()
            addPermissionToGroup()
        }
    }

    fun isAValidPlayer(pl: OfflinePlayer): IsValid {
        val user = api.userManager.getUser(pl.uniqueId)
        val valid = (pl.hasPlayedBefore() || pl.isOnline) && user != null
        return if (valid) {
            IsValid(true, user)
        } else {
            IsValid(false, null)
        }
    }

    fun grantPermissions(pl: OfflinePlayer): ErrorType = grantPermissionsToPlayer(pl, "wh2sperx.admin")

    fun grantPermissionsToPlayer(pl: OfflinePlayer, node: String, value: Boolean? = true): ErrorType {
        val player = isAValidPlayer(pl)
        if(!player.valid) return ErrorType.UNKNOWN_PLAYER
        val user = player.user ?: return ErrorType.UNKNOWN_PLAYER
        val node = Node.builder(node).value(value ?: true).build()
        if(alreadyHasPermission(user)) return ErrorType.ALREADY_HAVE_PERMISSION
        user.data().add(node)
        api.userManager.saveUser(user)
        return ErrorType.TRUE
    }

    fun revokePermissions(pl: OfflinePlayer) {
        revokePermissionFromPlayer(pl, "wh2sperx.admin")
    }

    fun revokePermissionFromPlayer(pl: OfflinePlayer, node: String) {
        val user = api.userManager.getUser(pl.uniqueId) ?: return
        val node = Node.builder(node).build()
        if (!alreadyHasPermission(user)) return
        user.data().remove(node)
        api.userManager.saveUser(user)
    }

    private fun createGroupIfNotExist() {
        if (api.groupManager.getGroup(groupName) == null) {
            api.groupManager.createAndLoadGroup(groupName).join()
        }
    }

    private fun addPermissionToGroup() {
        val group = api.groupManager.getGroup(groupName)
        if (group != null) {
            val node = PermissionNode.builder("wh2sperx.admin").build()
            group.data().add(node)
            api.groupManager.saveGroup(group)
        }
    }

    private fun alreadyHasPermission(user: User): Boolean {
        val hasPermission = user.cachedData.permissionData.checkPermission("wh2sperx.admin").asBoolean()
        return hasPermission
    }
}