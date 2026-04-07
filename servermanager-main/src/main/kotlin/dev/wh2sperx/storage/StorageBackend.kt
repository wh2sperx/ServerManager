package dev.wh2sperx.storage

import dev.wh2sperx.storage.model.PlayerData
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class StorageBackend(
    private val dataFolder: File,
    private val storageFileName: String
) {
    private var conn: Connection? = null

    fun initialize() {
        Class.forName("org.h2.Driver")
        val dbPath = File(dataFolder, storageFileName).absolutePath
        conn = DriverManager.getConnection("jdbc:h2:$dbPath", "sa", "")
        createTables()
    }

    fun shutdown() {
        conn?.close()
    }

    // ─── Table creation ──────────────────────────────────────────

    private fun createTables() {
        conn?.createStatement()?.use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS players (
                    uuid        VARCHAR(36)  PRIMARY KEY,
                    password    VARCHAR(255) NOT NULL,
                    issued_at   BIGINT       NOT NULL
                )
                """.trimIndent()
            )
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS pending_passwords (
                    uuid          VARCHAR(36)  PRIMARY KEY,
                    raw_password  VARCHAR(64)  NOT NULL,
                    created_at    BIGINT       NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    // ─── Players CRUD ────────────────────────────────────────────

    fun savePlayer(data: PlayerData) {
        val sql = "MERGE INTO players KEY(uuid) VALUES (?, ?, ?)"
        conn?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, data.uuid.toString())
            stmt.setString(2, data.password)
            stmt.setLong(3, data.issuedAt)
            stmt.executeUpdate()
        }
    }

    fun getPlayer(uuid: UUID): PlayerData? {
        val sql = "SELECT * FROM players WHERE uuid = ?"
        return conn?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeQuery().takeIf { it.next() }?.let { rs ->
                PlayerData(
                    uuid = UUID.fromString(rs.getString("uuid")),
                    password = rs.getString("password"),
                    issuedAt = rs.getLong("issued_at")
                )
            }
        }
    }

    fun deletePlayer(uuid: UUID) {
        conn?.prepareStatement("DELETE FROM players WHERE uuid = ?")?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeUpdate()
        }
    }

    fun playerExists(uuid: UUID): Boolean {
        val sql = "SELECT 1 FROM players WHERE uuid = ?"
        return conn?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeQuery().next()
        } ?: false
    }

    fun getAllPlayers(): List<PlayerData> {
        val list = mutableListOf<PlayerData>()
        conn?.prepareStatement("SELECT * FROM players ORDER BY issued_at DESC")?.use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    list.add(
                        PlayerData(
                            uuid = UUID.fromString(rs.getString("uuid")),
                            password = rs.getString("password"),
                            issuedAt = rs.getLong("issued_at")
                        )
                    )
                }
            }
        }
        return list
    }

    // ─── Pending passwords CRUD ──────────────────────────────────

    fun savePending(uuid: UUID, rawPassword: String) {
        val sql = "MERGE INTO pending_passwords KEY(uuid) VALUES (?, ?, ?)"
        conn?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.setString(2, rawPassword)
            stmt.setLong(3, System.currentTimeMillis())
            stmt.executeUpdate()
        }
    }

    fun getPending(uuid: UUID): String? {
        val sql = "SELECT raw_password FROM pending_passwords WHERE uuid = ?"
        return conn?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeQuery().takeIf { it.next() }?.getString("raw_password")
        }
    }

    fun deletePending(uuid: UUID) {
        conn?.prepareStatement("DELETE FROM pending_passwords WHERE uuid = ?")?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeUpdate()
        }
    }
}