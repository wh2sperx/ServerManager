package dev.wh2sperx.listener

import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

class AsyncChatListener(
    private val plugin: ServerManager
) : Listener {
    @EventHandler
    fun onAsyncChatEvent(event: AsyncChatEvent) {
        val pl = event.player
        val uuid = pl.uniqueId
        val att = plugin.configManager.retry
        var attempts = 0;
        if(isInQueue(uuid)) {
            event.isCancelled = true
            //todo: yeu cau nhap mat khau
            val password = PlainTextComponentSerializer.plainText().serialize(event.message())
            val pass = plugin.passwordManager.verifyPassword(uuid, password)
            if(pass) {
                FuckingSpecialModeManager.enableSpecialMode(uuid)
                plugin.messageManager.send(pl, "command.login-success")
                dequeue(uuid)
            } else {
                attempts++
                if(attempts >= att) {
                    plugin.messageManager.send(pl, "command.wrong-password")
                    dequeue(uuid)
                } else {
                    plugin.messageManager.send(pl, "command.wrong-password-retry", mapOf("retry" to (att - attempts).toString()))
                }
            }
        }
    }

    fun onAsyncChatEvent2(event: AsyncChatEvent) {
        val pl = event.player
        val uuid = pl.uniqueId
        if(FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            event.isCancelled = true
            val msg = PlainTextComponentSerializer.plainText().serialize(event.message()).split(" ")
            when(msg[0]) {
                "logout" -> {
                    FuckingSpecialModeManager.disableSpecialMode(uuid)
                }
            }
        }
    }

    companion object {
        private val fuckingQueue = HashSet<UUID>()

        fun isInQueue(uuid: UUID): Boolean = fuckingQueue.contains(uuid)
        fun putQueue(uuid: UUID) { fuckingQueue.add(uuid) }
        fun dequeue(uuid: UUID) { fuckingQueue.remove(uuid) }
    }
}