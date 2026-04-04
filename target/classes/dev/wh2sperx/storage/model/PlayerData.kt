package dev.wh2sperx.storage.model

import java.util.UUID

data class PlayerData(
    val uuid: UUID,
    val password: String,
    val issuedAt: Long
)
