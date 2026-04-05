package dev.wh2sperx.listener

import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.TabCompleteEvent

class TabCompleteListener(
    private val plugin: ServerManager
) : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onTabComplete(event: TabCompleteEvent) {
        val player = event.sender as? Player ?: return
        if (FuckingSpecialModeManager.isInSpecialMode(player.uniqueId)) {
            event.completions = emptyList()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onStaffCommand(command: PlayerCommandPreprocessEvent) {
        val pl = command.player
        if (FuckingSpecialModeManager.isInSpecialMode(pl.uniqueId)) {
            command.isCancelled = true
            plugin.messageManager.send(pl, "command.command-blocked-in-special-mode")
        }
    }
}