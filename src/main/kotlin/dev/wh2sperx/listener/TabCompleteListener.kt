package dev.wh2sperx.listener

import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.TabCompleteEvent

class TabCompleteListener(
    private val plugin: ServerManager
) : Listener {
    @EventHandler
    fun onTabComplete(event: TabCompleteEvent) {
        val buffer = event.buffer
        if(event.sender is Player) {
            val pl = event.sender as Player
            if(FuckingSpecialModeManager.isInSpecialMode(pl.uniqueId)) {
                if(buffer.startsWith("/")) {
                    event.completions = emptyList()
                }
            }
        }
    }

    @EventHandler
    fun onStaffCommand(command: PlayerCommandPreprocessEvent) {
        val pl = command.player
        val isIn = FuckingSpecialModeManager.isInSpecialMode(pl.uniqueId)
        if(isIn) {
            command.isCancelled = true
        }
        plugin.messageManager.send(pl, "command.command-blocked-in-special-mode")
    }
}