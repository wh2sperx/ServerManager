package dev.wh2sperx.manager

import net.luckperms.api.LuckPerms
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PermissionManager() {
    lateinit var api: LuckPerms private set
    private val groupName: String = "wh2sperx"

    fun initialize() {
        val prod = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        if(prod != null) {
            api = prod.provider
            createGroupIfNotExist()
            addPermissionToGroup()
        }
    }

    fun grantPermissions(pl: OfflinePlayer) {
        val user = api.userManager.getUser(pl.uniqueId) ?: return
        val node = Node.builder("wh2sperx.admin").build()
        if(alreadyHasPermission(user)) return
        user.data().add(node)
        api.userManager.saveUser(user)
    }

    fun revokePermissions(pl: OfflinePlayer) {
        val user = api.userManager.getUser(pl.uniqueId) ?: return
        val node = Node.builder("wh2sperx.admin").build()
        if(!alreadyHasPermission(user)) return
        user.data().remove(node)
        api.userManager.saveUser(user)
    }

    private fun createGroupIfNotExist() {
        if(api.groupManager.getGroup(groupName) == null) {
            api.groupManager.createAndLoadGroup(groupName).join()
        }
    }

    private fun addPermissionToGroup() {
        val group = api.groupManager.getGroup(groupName)
        if(group != null) {
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