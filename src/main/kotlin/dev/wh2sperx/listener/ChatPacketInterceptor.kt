package dev.wh2sperx.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatPacketInterceptor(private val serverManager: ServerManager) : PacketAdapter(
    serverManager,
    ListenerPriority.HIGHEST,
    PacketType.Play.Client.CHAT
) {
    private val loginAttempts = ConcurrentHashMap<UUID, Int>()

    override fun onPacketReceiving(event: PacketEvent) {
        val player = event.player
        val uuid = player.uniqueId
        if (isInQueue(uuid) || FuckingSpecialModeManager.isInSpecialMode(uuid)) {
            event.isCancelled = true
            val packet = event.packet
            val message = packet.strings.read(0)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (isInQueue(uuid)) {
                    handleLoginPacket(player, message, uuid)
                } else if (FuckingSpecialModeManager.isInSpecialMode(uuid)) {
                    handleSpecialModePacket(player, message)
                }
            })
        }
    }

    private fun handleLoginPacket(player: Player, message: String, uuid: UUID) {
        val maxRetry = serverManager.configManager.retry
        val password = message.split(" ")[0]
        val pass = serverManager.passwordManager.verifyPassword(uuid, password)
        if (pass) {
            dequeue(uuid)
            FuckingSpecialModeManager.enableSpecialMode(uuid)
            serverManager.messageManager.send(player, "command.login-success")
            loginAttempts.remove(uuid)
        } else {
            val currentAttempts = loginAttempts.getOrDefault(uuid, 0) + 1
            loginAttempts[uuid] = currentAttempts
            if (currentAttempts >= maxRetry) {
                serverManager.messageManager.send(player, "command.wrong-password")
                dequeue(uuid)
                loginAttempts.remove(uuid)
            } else {
                val remaining = maxRetry - currentAttempts
                serverManager.messageManager.send(
                    player, "command.wrong-password-retry",
                    mapOf("retry" to remaining.toString())
                )
            }
        }
    }

    private fun handleSpecialModePacket(player: Player, message: String) {
        val args = message.split(" ")
        if (args.isEmpty() || args[0].isEmpty()) return
        when (args[0].lowercase()) {
            "logout", "quit", "exit" -> handleLogout(player)
            else -> {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    player.sendMessage("lenh deo hop le")
                })
            }
        }
    }

    private fun handleLogout(player: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            FuckingSpecialModeManager.disableSpecialMode(player.uniqueId)
            player.sendMessage("da dang xuat")
        })
    }

    companion object {
        private val fuckingQueue = ConcurrentHashMap.newKeySet<UUID>()

        fun isInQueue(uuid: UUID): Boolean = fuckingQueue.contains(uuid)
        fun putQueue(uuid: UUID) = fuckingQueue.add(uuid)
        fun dequeue(uuid: UUID) = fuckingQueue.remove(uuid)
    }
}
