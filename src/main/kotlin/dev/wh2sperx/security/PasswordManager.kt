package dev.wh2sperx.security

import dev.wh2sperx.storage.StorageManager
import dev.wh2sperx.storage.model.PlayerData
import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import java.util.UUID

class PasswordManager(
    private val storageManager: StorageManager
) {
    private val random = SecureRandom()

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
        val stored = storageManager.getPlayerData(uuid) ?: return false
        return BCrypt.checkpw(rawPassword, stored.password)
    }

    fun isAccountExists(uuid: UUID): Boolean = storageManager.isPlayerExists(uuid)
}
