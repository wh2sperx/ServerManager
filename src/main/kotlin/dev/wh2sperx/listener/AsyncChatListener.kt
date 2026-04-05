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
    private val loginAttempts = mutableMapOf<UUID, Int>()

    @EventHandler
    fun onAsyncChatEvent(event: AsyncChatEvent) {
        val pl = event.player
        val uuid = pl.uniqueId
        val maxRetry = plugin.configManager.retry

        if(isInQueue(uuid)) {
            event.isCancelled = true

            val password = PlainTextComponentSerializer.plainText().serialize(event.message())
            val pass = plugin.passwordManager.verifyPassword(uuid, password)

            if(pass) {
                FuckingSpecialModeManager.enableSpecialMode(uuid)
                plugin.messageManager.send(pl, "command.login-success")
                dequeue(uuid)
                loginAttempts.remove(uuid)
            } else {
                val currentAttempts = loginAttempts.getOrDefault(uuid, 0) + 1
                loginAttempts[uuid] = currentAttempts

                if(currentAttempts >= maxRetry) {
                    plugin.messageManager.send(pl, "command.wrong-password")
                    dequeue(uuid)
                    loginAttempts.remove(uuid)
                } else {
                    val remaining = maxRetry - currentAttempts
                    plugin.messageManager.send(pl, "command.wrong-password-retry",
                        mapOf("retry" to remaining.toString()))
                }
            }
        }
    }

    @EventHandler
    fun onAsyncChatEvent2(event: AsyncChatEvent) {
        val pl = event.player
        val uuid = pl.uniqueId
        if(FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            event.isCancelled = true
            val msg = PlainTextComponentSerializer.plainText().serialize(event.message()).split(" ")
            if(msg.isEmpty()) return
            when(msg[0]) {
                "logout", "quit", "exit" -> {
                    FuckingSpecialModeManager.disableSpecialMode(uuid)
                    pl.sendMessage("da dang xuat") //debug
                }
            }
        }
    }

    companion object {
        private val fuckingQueue = HashSet<UUID>()

        fun isInQueue(uuid: UUID): Boolean = fuckingQueue.contains(uuid)
        fun putQueue(uuid: UUID) { fuckingQueue.add(uuid) }
        fun dequeue(uuid: UUID) { fuckingQueue.remove(uuid) }
        fun addToQueueIfAbsent(uuid: UUID) {
            if(!isInQueue(uuid)) putQueue(uuid)
        }
    }
}