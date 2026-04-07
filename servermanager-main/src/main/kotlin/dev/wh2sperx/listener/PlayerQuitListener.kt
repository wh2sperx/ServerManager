package dev.wh2sperx.listener

import dev.wh2sperx.manager.FuckingSpecialModeManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        if(FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            FuckingSpecialModeManager.disableSpecialMode(uuid)
        }
    }
}