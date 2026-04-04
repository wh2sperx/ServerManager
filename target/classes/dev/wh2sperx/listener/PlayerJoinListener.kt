package dev.wh2sperx.listener

import dev.wh2sperx.ServerManager
import fr.xephi.authme.events.LoginEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerJoinListener(
    private val plugin: ServerManager
) : Listener {

    @EventHandler
    fun onPlayerLogin(event: LoginEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val pendingPassword = plugin.storageManager.getPendingPassword(uuid) ?: return
        plugin.messageManager.sendPasswordAnnouncement(player, pendingPassword)
        plugin.storageManager.deletePendingPassword(uuid)
    }
}