package dev.wh2sperx.api

import dev.wh2sperx.ServerManager
import dev.wh2sperx.listener.ChatPacketInterceptor
import dev.wh2sperx.manager.FuckingSpecialModeManager
import java.util.*

class ServerManagerAPIImplement(private val plugin: ServerManager) : ServerManagerAPI {
    override fun enableSpecialMode(uuid: UUID) {
        FuckingSpecialModeManager.enableSpecialMode(uuid)
    }

    override fun disableSpecialMode(uuid: UUID) {
        FuckingSpecialModeManager.disableSpecialMode(uuid)
    }

    override fun toggleSpecialMode(uuid: UUID) {
        FuckingSpecialModeManager.toggleSpecialMode(uuid)
    }

    override fun isInSpecialMode(uuid: UUID): Boolean {
        return FuckingSpecialModeManager.isInSpecialMode(uuid)
    }

    override fun isInPasswordQueue(uuid: UUID): Boolean {
        return ChatPacketInterceptor.isInQueue(uuid)
    }

    override fun addToPasswordQueue(uuid: UUID) {
        ChatPacketInterceptor.putQueue(uuid)
    }

    override fun removeFromPasswordQueue(uuid: UUID) {
        ChatPacketInterceptor.dequeue(uuid)
    }

    override fun getChatState(): Boolean {
        return ChatPacketInterceptor.chatState
    }
}