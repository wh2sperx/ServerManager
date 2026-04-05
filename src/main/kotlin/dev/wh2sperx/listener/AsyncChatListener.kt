package dev.wh2sperx.listener

import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AsyncChatListener(
    private val plugin: ServerManager
) : Listener {
    private val loginAttempts = ConcurrentHashMap<UUID, Int>()

    //Debugging
    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncChatEvent(event: AsyncChatEvent) {
        val pl = event.player
        val uuid = pl.uniqueId
        val msg = PlainTextComponentSerializer.plainText().serialize(event.originalMessage())

        plugin.logger.info("=== EVENT START ===")
        plugin.logger.info("Message: '$msg'")
        plugin.logger.info("isInQueue: ${isInQueue(uuid)}")
        plugin.logger.info("isInSpecialMode: ${FuckingSpecialModeManager.isInSpecialMode(uuid)}")

        if (isInQueue(uuid)) {
            plugin.logger.info("→ Case QUEUE - processing login")
            event.isCancelled = true
            event.viewers().clear()
            handleLogin(event, pl, uuid)
            plugin.logger.info(
                "→ After handleLogin - isInQueue: ${isInQueue(uuid)}, isInSpecialMode: ${
                    FuckingSpecialModeManager.isInSpecialMode(
                        uuid
                    )
                }"
            )
            return
        }

        if (FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            plugin.logger.info("→ Case SPECIAL MODE - processing command")
            event.isCancelled = true
            event.viewers().clear()
            handleSpecialModeCommands(event, pl)
            return
        }

        plugin.logger.info("→ NO CASE matched")
    }

    private fun handleLogin(event: AsyncChatEvent, pl: Player, uuid: UUID) {
        val maxRetry = plugin.configManager.retry
        val password = PlainTextComponentSerializer.plainText().serialize(event.originalMessage()).split(" ")[0]
        val pass = plugin.passwordManager.verifyPassword(uuid, password)

        if (pass) {
            dequeue(uuid)
            FuckingSpecialModeManager.enableSpecialMode(uuid)
            plugin.messageManager.send(pl, "command.login-success")
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
        val msg2 = PlainTextComponentSerializer.plainText().serialize(event.originalMessage())
        val args = msg2.split(" ")

        if (args.isEmpty() || args[0].isEmpty()) return
        plugin.logger.info("Special mode receiver: ${args[0]}") //debug

        when (args[0].lowercase()) {
            "logout", "quit", "exit" -> handleLogout(pl)
            else -> {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    pl.sendMessage("lenh deo hop le")
                    //debug
                    plugin.logger.info("Debug: lenh deo hop le")
                })
            }
        }
    }

    private fun handleLogout(player: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            FuckingSpecialModeManager.disableSpecialMode(player.uniqueId)
            player.sendMessage("da dang xuat")
            //debug
            plugin.logger.info("=== LOGOUT ===")
            plugin.logger.info("Special mode disabled for ${player.name}")
        })
    }

    companion object {
        private val fuckingQueue = ConcurrentHashMap.newKeySet<UUID>()

        fun isInQueue(uuid: UUID): Boolean = fuckingQueue.contains(uuid)
        fun putQueue(uuid: UUID) = fuckingQueue.add(uuid)
        fun dequeue(uuid: UUID) = fuckingQueue.remove(uuid)
    }
}