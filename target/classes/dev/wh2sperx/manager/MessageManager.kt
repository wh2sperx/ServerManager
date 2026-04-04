package dev.wh2sperx.manager

import dev.wh2sperx.config.ConfigManager
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

class MessageManager(
    private val adventure: BukkitAudiences,
    private val configManager: ConfigManager,
    private val hasPapi: Boolean
) {
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    fun send(player: Player, key: String, placeholders: Map<String, String> = emptyMap()) {
        val component = getMessage(key, placeholders, player)
        adventure.player(player).sendMessage(component)
    }

    fun sendConsole(key: String, placeholders: Map<String, String> = emptyMap()) {
        val component = getMessage(key, placeholders)
        adventure.console().sendMessage(component)
    }

    fun sendComponent(player: Player, component: Component) {
        adventure.player(player).sendMessage(component)
    }

    fun sendConsoleComponent(component: Component) {
        adventure.console().sendMessage(component)
    }

    fun sendPasswordAnnouncement(player: Player, password: String) {
        send(player, "password.header")

        val rawMsg = replacePlaceholders(
            configManager.getMessage("password.body"),
            mapOf("password" to password),
            player
        )
        val component = miniMessage.deserialize(rawMsg)
            .clickEvent(ClickEvent.copyToClipboard(password))
            .hoverEvent(HoverEvent.showText(
                miniMessage.deserialize(configManager.getMessage("password.hover"))
            ))
        adventure.player(player).sendMessage(component)

        send(player, "password.footer")
    }

    fun getMessage(key: String, placeholders: Map<String, String> = emptyMap(), player: Player? = null): Component {
        val raw = replacePlaceholders(configManager.getMessage(key), placeholders, player)
        return miniMessage.deserialize(raw)
    }

    fun deserialize(raw: String): Component = miniMessage.deserialize(raw)

    // Internal

    private fun replacePlaceholders(
        message: String,
        placeholders: Map<String, String>,
        player: Player? = null
    ): String {
        var result = message
        placeholders.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        if (hasPapi && player != null) {
            result = PlaceholderAPI.setPlaceholders(player, result)
        }
        return result
    }
}