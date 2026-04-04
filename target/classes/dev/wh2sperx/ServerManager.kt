package dev.wh2sperx

import dev.wh2sperx.command.AdminCommand
import dev.wh2sperx.config.ConfigManager
import dev.wh2sperx.listener.PlayerJoinListener
import dev.wh2sperx.manager.MessageManager
import dev.wh2sperx.security.PasswordManager
import dev.wh2sperx.storage.StorageManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ServerManager : JavaPlugin() {
    private var hasVault: Boolean = false
    private var hasPapi: Boolean = false

    lateinit var economy: Economy private set
    lateinit var adventure: BukkitAudiences private set
    lateinit var configManager: ConfigManager private set
    lateinit var storageManager: StorageManager private set
    lateinit var messageManager: MessageManager private set
    lateinit var passwordManager: PasswordManager private set

    override fun onEnable() {
        hasVault = setupEconomy()
        hasPapi = checkPlaceholderAPI()

        adventure = BukkitAudiences.create(this)

        configManager = ConfigManager(this)
        storageManager = StorageManager(this)
        storageManager.initialize()
        messageManager = MessageManager(adventure, configManager, hasPapi)
        passwordManager = PasswordManager(storageManager)

        getCommand("admin")?.let { cmd ->
            val adminCommand = AdminCommand(this)
            cmd.setExecutor(adminCommand)
            cmd.tabCompleter = adminCommand
        }

        Bukkit.getPluginManager().registerEvents(PlayerJoinListener(this), this)
    }

    override fun onDisable() {
        if (::storageManager.isInitialized) storageManager.shutdown()
        if (::adventure.isInitialized) adventure.close()
    }

    private fun setupEconomy(): Boolean {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false
        val rsp = Bukkit.getServicesManager()
            .getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return true
    }

    private fun checkPlaceholderAPI(): Boolean {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI")?.isEnabled == true
    }
}
