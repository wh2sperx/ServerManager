package dev.wh2sperx.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.wh2sperx.ServerManager
import dev.wh2sperx.command.AdminCommand
import dev.wh2sperx.manager.FuckingSpecialModeManager

class LockChatPacketInterceptor(
    serverManager: ServerManager,
    private val ownerName: String
) : PacketAdapter(
    serverManager,
    ListenerPriority.HIGHEST,
    PacketType.Play.Client.CHAT
) {
    override fun onPacketReceiving(event: PacketEvent) {
        val p = event.player
        val uuid = p.uniqueId
        if(!ChatPacketInterceptor.chatState) {
            if(!(FuckingSpecialModeManager.isInSpecialMode(uuid) || AdminCommand.isOwner(p, ownerName))) {
                event.isCancelled = true
            }
        }
    }
}