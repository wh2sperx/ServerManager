package dev.wh2sperx.manager

import dev.wh2sperx.ServerManager
import dev.wh2sperx.config.ConfigManager
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MessageManager(
    private val configManager: ConfigManager,
    private val hasPapi: Boolean,
    private val plugin: ServerManager
) {
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    fun send(player: Player, key: String, placeholders: Map<String, String> = emptyMap()) {
        val component = getMessage(key, placeholders, player)
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.sendMessage(component)
        })
    }

    fun sendConsoleComponent(component: Component) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            Bukkit.getConsoleSender().sendMessage(component)
        })
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
            .hoverEvent(
                HoverEvent.showText(
                    miniMessage.deserialize(configManager.getMessage("password.hover"))
                )
            )
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.sendMessage(component)
        })

        send(player, "password.footer")
    }

    fun getMessage(key: String, placeholders: Map<String, String> = emptyMap(), player: Player? = null): Component {
        val raw = replacePlaceholders(configManager.getMessage(key), placeholders, player)
        return miniMessage.deserialize(raw)
    }

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