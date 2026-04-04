package dev.wh2sperx.manager

import org.bukkit.entity.Player
import java.util.UUID

class FuckingSpecialModeManager() {
    companion object {
        private val specialModePlayerList = HashSet<UUID>()

        fun enableSpecialMode(uuid: UUID) { specialModePlayerList.add(uuid) }

        fun disableSpecialMode(uuid: UUID) { specialModePlayerList.remove(uuid) }

        fun isInSpecialMode(uuid: UUID): Boolean = specialModePlayerList.contains(uuid)

        fun toggleSpecialMode(uuid: UUID): Boolean {
            return if(isInSpecialMode(uuid)) {
                disableSpecialMode(uuid)
                false
            } else {
                enableSpecialMode(uuid)
                true
            }
        }
    }
}