package dev.wh2sperx.manager

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FuckingSpecialModeManager {
    companion object {
        private val specialModePlayerList = ConcurrentHashMap.newKeySet<UUID>()

        fun enableSpecialMode(uuid: UUID) {
            specialModePlayerList.add(uuid)
        }

        fun disableSpecialMode(uuid: UUID) {
            specialModePlayerList.remove(uuid)
        }

        fun isInSpecialMode(uuid: UUID): Boolean =
            specialModePlayerList.contains(uuid)

        fun toggleSpecialMode(uuid: UUID): Boolean {
            return if (isInSpecialMode(uuid)) {
                disableSpecialMode(uuid)
                false
            } else {
                enableSpecialMode(uuid)
                true
            }
        }
    }
}