package dev.wh2sperx.security

import dev.wh2sperx.ServerManager
import dev.wh2sperx.storage.model.PlayerData
import org.bukkit.Bukkit
import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import java.util.*

class PasswordManager(
    private val plugin: ServerManager
) {
    private val random = SecureRandom()

    fun initializeOwnerAccount(ownerName: String) {
        val offlinePlayer = Bukkit.getOfflinePlayer(ownerName)
        val uuid = offlinePlayer.uniqueId
        if (plugin.storageManager.isPlayerExists(uuid)) return
        val rawPassword = generateRandomPassword(
            plugin.configManager.passwordMinLength,
            plugin.configManager.passwordMaxLength
        )
        val playerData = createPlayerData(uuid, rawPassword)
        plugin.storageManager.savePlayerData(playerData)
        plugin.permissionManager.grantPermissions(offlinePlayer)
        Bukkit.getPlayer(uuid)?.let { onlinePlayer ->
            plugin.messageManager.sendPasswordAnnouncement(onlinePlayer, rawPassword)
        } ?: plugin.storageManager.savePendingPassword(uuid, rawPassword)
    }

    fun generateRandomPassword(minLength: Int = 8, maxLength: Int = 12): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val allChars = upper + lower + digits
        val length = minLength + random.nextInt(maxLength - minLength + 1)

        val required = mutableListOf(
            upper[random.nextInt(upper.length)],
            lower[random.nextInt(lower.length)],
            digits[random.nextInt(digits.length)]
        )

        val remaining = (1..(length - required.size)).map { allChars[random.nextInt(allChars.length)] }

        return (required + remaining).shuffled(random).joinToString("")
    }

    fun createPlayerData(uuid: UUID, rawPassword: String): PlayerData {
        return PlayerData(
            uuid = uuid,
            password = BCrypt.hashpw(rawPassword, BCrypt.gensalt()),
            issuedAt = System.currentTimeMillis() / 1000
        )
    }

    fun verifyPassword(uuid: UUID, rawPassword: String): Boolean {
        val stored = plugin.storageManager.getPlayerData(uuid) ?: return false
        return BCrypt.checkpw(rawPassword, stored.password)
    }

    fun isAccountExists(uuid: UUID): Boolean = plugin.storageManager.isPlayerExists(uuid)
}
