package dev.wh2sperx.listener

import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.*

class AsyncChatListener(
    private val plugin: ServerManager
) : Listener {
    private val loginAttempts = mutableMapOf<UUID, Int>()

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncChatEvent(event: AsyncChatEvent) {
        val pl = event.player
        val uuid = pl.uniqueId
        if (isInQueue(uuid)) {
            event.viewers().clear()
            handleLogin(event, pl, uuid)
            return
        }
        if (FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            event.viewers().clear()
            handleSpecialModeCommands(event, pl)
            return
        }
    }

    private fun handleLogin(event: AsyncChatEvent, pl: Player, uuid: UUID) {
        val maxRetry = plugin.configManager.retry
        val password = LegacyComponentSerializer.legacySection().serialize(event.originalMessage())
        val pass = plugin.passwordManager.verifyPassword(uuid, password)

        if (pass) {
            FuckingSpecialModeManager.enableSpecialMode(uuid)
            plugin.messageManager.send(pl, "command.login-success")
            dequeue(uuid)
            loginAttempts.remove(uuid)
        } else {
            val currentAttempts = loginAttempts.getOrDefault(uuid, 0) + 1
            loginAttempts[uuid] = currentAttempts

            if (currentAttempts >= maxRetry) {
                plugin.messageManager.send(pl, "command.wrong-password")
                dequeue(uuid)
                loginAttempts.remove(uuid)
            } else {
                val remaining = maxRetry - currentAttempts
                plugin.messageManager.send(
                    pl, "command.wrong-password-retry",
                    mapOf("retry" to remaining.toString())
                )
            }
        }
    }

    private fun handleSpecialModeCommands(event: AsyncChatEvent, pl: Player) {
        val msg = event.originalMessage()
        val msg2 = LegacyComponentSerializer.legacySection().serialize(msg)
        val args = msg2.split(" ")

        if (args.isEmpty() || args[0].isEmpty()) return

        when (args[0].lowercase()) {
            "logout", "quit", "exit" -> handleLogout(pl)
            else -> {
                pl.sendMessage("lenh deo hop le")
            }
        }
    }

    private fun handleLogout(player: Player) {
        FuckingSpecialModeManager.disableSpecialMode(player.uniqueId)
        player.sendMessage("da dang xuat") //debug
    }

    companion object {
        private val fuckingQueue = HashSet<UUID>()

        fun isInQueue(uuid: UUID): Boolean = fuckingQueue.contains(uuid)
        fun putQueue(uuid: UUID) {
            fuckingQueue.add(uuid)
        }

        fun dequeue(uuid: UUID) {
            fuckingQueue.remove(uuid)
        }

        fun addToQueueIfAbsent(uuid: UUID) {
            if (!isInQueue(uuid)) putQueue(uuid)
        }
    }
}