package dev.wh2sperx

import dev.wh2sperx.command.AdminCommand
import dev.wh2sperx.config.ConfigManager
import dev.wh2sperx.listener.AsyncChatListener
import dev.wh2sperx.listener.PlayerJoinListener
import dev.wh2sperx.listener.TabCompleteListener
import dev.wh2sperx.manager.MessageManager
import dev.wh2sperx.manager.PermissionManager
import dev.wh2sperx.metrics.MetricsManager
import dev.wh2sperx.security.PasswordManager
import dev.wh2sperx.storage.StorageManager
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

private const val PLUGIN_ID = 30571

class ServerManager : JavaPlugin() {
    private var hasVault: Boolean = false
    private var hasPapi: Boolean = false

    lateinit var ownerAccount: String private set
    lateinit var economy: Economy private set
    lateinit var configManager: ConfigManager private set
    lateinit var storageManager: StorageManager private set
    lateinit var messageManager: MessageManager private set
    lateinit var passwordManager: PasswordManager private set
    lateinit var permissionManager: PermissionManager private set
    lateinit var metricsManager: MetricsManager private set

    override fun onEnable() {
        // Dependencies
        hasVault = setupEconomy()
        hasPapi = checkPlaceholderAPI()

        // Initial ConfigManager
        configManager = ConfigManager(this)
        ownerAccount = configManager.ownerName

        // Initial Storage
        storageManager = StorageManager(this)
        storageManager.initialize()

        // Stuff
        messageManager = MessageManager(configManager, hasPapi, this)

        // Permission
        permissionManager = PermissionManager(); permissionManager.initialize()

        // Metrics ( bstats )
        metricsManager = MetricsManager(
            PLUGIN_ID,
            this
        )
        metricsManager.start()

        // Initial PasswordManager
        passwordManager = PasswordManager(this)
        passwordManager.initializeOwnerAccount(ownerAccount)

        // Register Command
        getCommand("admin")?.let { cmd ->
            val adminCommand = AdminCommand(this, ownerAccount)
            cmd.setExecutor(adminCommand)
            cmd.tabCompleter = adminCommand
        }

        // Register Event
        Bukkit.getPluginManager().registerEvents(AsyncChatListener(this), this)
        Bukkit.getPluginManager().registerEvents(PlayerJoinListener(this), this)
        Bukkit.getPluginManager().registerEvents(TabCompleteListener(this), this)
    }

    override fun onDisable() {
        if (::storageManager.isInitialized) storageManager.shutdown()
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
