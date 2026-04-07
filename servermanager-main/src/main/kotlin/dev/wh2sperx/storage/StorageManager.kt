package dev.wh2sperx.storage

import dev.wh2sperx.ServerManager
import dev.wh2sperx.storage.model.PlayerData
import java.util.*

class StorageManager(
    private val plugin: ServerManager
) {
    private lateinit var backend: StorageBackend

    fun initialize() {
        backend = StorageBackend(plugin.dataFolder, plugin.configManager.storageFile)
        backend.initialize()
    }

    fun shutdown() = backend.shutdown()

    // ─── Player data ─────────────────────────────────────────────

    fun getPlayerData(uuid: UUID): PlayerData? = backend.getPlayer(uuid)
    fun savePlayerData(data: PlayerData) = backend.savePlayer(data)
    fun deletePlayerData(uuid: UUID) = backend.deletePlayer(uuid)
    fun isPlayerExists(uuid: UUID): Boolean = backend.playerExists(uuid)
    fun getAllPlayers(): List<PlayerData> = backend.getAllPlayers()

    // ─── Pending passwords ───────────────────────────────────────

    fun savePendingPassword(uuid: UUID, rawPassword: String) = backend.savePending(uuid, rawPassword)
    fun getPendingPassword(uuid: UUID): String? = backend.getPending(uuid)
    fun deletePendingPassword(uuid: UUID) = backend.deletePending(uuid)
}