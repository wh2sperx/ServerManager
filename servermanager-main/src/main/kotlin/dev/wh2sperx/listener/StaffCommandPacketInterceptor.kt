package dev.wh2sperx.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.wh2sperx.ServerManager
import dev.wh2sperx.command.AdminCommand
import dev.wh2sperx.manager.FuckingSpecialModeManager

class StaffCommandPacketInterceptor(private val serverManager: ServerManager) : PacketAdapter(
    serverManager,
    ListenerPriority.HIGHEST,
    PacketType.Play.Client.TAB_COMPLETE,
    PacketType.Play.Client.CLIENT_COMMAND
) {
    override fun onPacketReceiving(event: PacketEvent) {
        val player = event.player
        val uuid = player.uniqueId
        if (FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            when (event.packet.type) {
                PacketType.Play.Client.CLIENT_COMMAND -> {
                    event.isCancelled = true
                    serverManager.messageManager.send(player, "command.command-blocked-in-special-mode")
                }
                PacketType.Play.Client.TAB_COMPLETE -> {
                    event.isCancelled = true
                }
            }
        }
    }

    override fun onPacketSending(event: PacketEvent) {
        if(!ChatPacketInterceptor.chatState) {
            event.isCancelled = true
        }
    }
}