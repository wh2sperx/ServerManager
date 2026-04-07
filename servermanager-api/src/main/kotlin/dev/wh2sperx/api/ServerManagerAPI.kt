package dev.wh2sperx.api

import java.util.*

interface ServerManagerAPI {
    fun enableSpecialMode(uuid: UUID)
    fun disableSpecialMode(uuid: UUID)
    fun toggleSpecialMode(uuid: UUID)
    fun isInSpecialMode(uuid: UUID): Boolean

    fun isInPasswordQueue(uuid: UUID): Boolean
    fun addToPasswordQueue(uuid: UUID)
    fun removeFromPasswordQueue(uuid: UUID)
    fun getChatState(): Boolean
}