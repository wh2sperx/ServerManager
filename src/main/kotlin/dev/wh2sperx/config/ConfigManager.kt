package dev.wh2sperx.config

import dev.wh2sperx.ServerManager
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(
    private val plugin: ServerManager
) {
    private lateinit var config: FileConfiguration
    private lateinit var messagesConfig: YamlConfiguration
    private lateinit var messagesFile: File

    init {
        plugin.saveDefaultConfig()
        plugin.saveResource("messages.yml", false)
        reload()
    }

    fun reload() {
        plugin.reloadConfig()
        config = plugin.config

        messagesFile = File(plugin.dataFolder, "messages.yml")
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile)
    }

    // Config values

    val storageFile: String
        get() = config.getString("storage.file", "data")!!

    val passwordMinLength: Int
        get() = config.getInt("password.min-length", 8)

    val passwordMaxLength: Int
        get() = config.getInt("password.max-length", 12)

    val listPageSize: Int
        get() = config.getInt("list.page-size", 10)

    val metrics: Boolean
        get() = config.getBoolean("metrics.toggle")

    val retry: Int
        get() = config.getInt("password.retry")

    val ownerName: String
        get() = config.getString("configure.owner", "qhuy")!!

    // Messages

    fun getMessage(key: String): String {
        return messagesConfig.getString(key, "<red>Missing message: $key")!!
    }
}