package dev.wh2sperx.command

import dev.wh2sperx.ServerManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class AdminCommand(
    private val plugin: ServerManager
) : CommandExecutor, TabCompleter {

    companion object {
        private const val OWNER_NAME = "qhuy"
    }

    private val storage get() = plugin.storageManager
    private val passwords get() = plugin.passwordManager
    private val messages get() = plugin.messageManager
    private val config get() = plugin.configManager

    private fun isOwner(sender: CommandSender): Boolean =
        sender.name.equals(OWNER_NAME, ignoreCase = false)

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("wh2sperx.admin") && !isOwner(sender)) {
            messages.send(sender, "command.no-permission")
            return true
        }

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "login" -> handleLogin(sender, args)
            "list", "grant", "revoke", "reload" -> {
                if (!isOwner(sender)) {
                    messages.send(sender, "command.no-permission")
                    return true
                }
                when (args[0].lowercase()) {
                    "list" -> handleList(sender, args)
                    "grant" -> handleGrant(sender, args)
                    "revoke" -> handleRevoke(sender, args)
                    "reload" -> handleReload(sender)
                }
            }
            else -> sendHelp(sender)
        }
        return true
    }

    // Subcommands

    private fun handleList(sender: CommandSender, args: Array<out String>) {
        val allPlayers = storage.getAllPlayers()

        if (allPlayers.isEmpty()) {
            messages.send(sender, "list.empty")
            return
        }

        val pageSize = config.listPageSize
        val totalPages = (allPlayers.size + pageSize - 1) / pageSize
        val page = if (args.size >= 2) {
            (args[1].toIntOrNull() ?: 1).coerceIn(1, totalPages)
        } else 1

        val start = (page - 1) * pageSize
        val end = (start + pageSize).coerceAtMost(allPlayers.size)
        val pageItems = allPlayers.subList(start, end)

        // Header
        messages.send(sender, "list.header", mapOf(
            "page" to page.toString(),
            "total_pages" to totalPages.toString(),
            "total_players" to allPlayers.size.toString()
        ))

        // Entries
        for ((index, data) in pageItems.withIndex()) {
            val offlinePlayer = Bukkit.getOfflinePlayer(data.uuid)
            val name = offlinePlayer.name ?: data.uuid.toString()
            messages.send(sender, "list.entry", mapOf(
                "index" to (start + index + 1).toString(),
                "player" to name,
                "uuid" to data.uuid.toString()
            ))
        }

        // Footer
        messages.send(sender, "list.footer", mapOf(
            "page" to page.toString(),
            "total_pages" to totalPages.toString()
        ))
    }

    private fun handleGrant(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            messages.send(sender, "command.usage-grant")
            return
        }

        val targetName = args[1]
        val offlinePlayer = Bukkit.getOfflinePlayer(targetName)

        // Must have played before
        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline) {
            messages.send(sender, "command.player-never-joined", mapOf("player" to targetName))
            return
        }

        val uuid = offlinePlayer.uniqueId

        // Already has password
        if (passwords.isAccountExists(uuid)) {
            messages.send(sender, "command.already-granted", mapOf("player" to targetName))
            return
        }

        // Generate random password
        val rawPassword = passwords.generateRandomPassword(
            config.passwordMinLength,
            config.passwordMaxLength
        )

        // Hash + save to DB
        val playerData = passwords.createPlayerData(uuid, rawPassword)
        storage.savePlayerData(playerData)

        val onlineTarget = offlinePlayer.player
        if (onlineTarget != null && onlineTarget.isOnline) {
            // Player is online → send immediately
            messages.sendPasswordAnnouncement(onlineTarget, rawPassword)
            messages.send(sender, "command.grant-success-online", mapOf("player" to targetName))
        } else {
            // Player is offline → save pending
            storage.savePendingPassword(uuid, rawPassword)
            messages.send(sender, "command.grant-success-offline", mapOf("player" to targetName))
        }
    }

    private fun handleRevoke(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            messages.send(sender, "command.usage-revoke")
            return
        }

        val targetName = args[1]
        val offlinePlayer = Bukkit.getOfflinePlayer(targetName)
        val uuid = offlinePlayer.uniqueId

        if (!passwords.isAccountExists(uuid)) {
            messages.send(sender, "command.not-granted", mapOf("player" to targetName))
            return
        }

        storage.deletePlayerData(uuid)
        storage.deletePendingPassword(uuid)
        messages.send(sender, "command.revoke-success", mapOf("player" to targetName))
    }

    private fun handleLogin(sender: CommandSender, args: Array<out String>) {
        // TODO: Implement login logic
        messages.send(sender, "command.not-implemented")
    }

    private fun handleReload(sender: CommandSender) {
        config.reload()
        messages.send(sender, "command.reload-success")
    }

    private fun sendHelp(sender: CommandSender) {
        messages.send(sender, "command.help")
    }

    // Tab Complete

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (!sender.hasPermission("wh2sperx.admin") && !isOwner(sender)) return emptyList()

        return when (args.size) {
            1 -> {
                val allCommands = listOf("list", "grant", "revoke", "login", "reload")
                val filteredCommands = if (isOwner(sender)) {
                    allCommands
                } else {
                    listOf("login")
                }
                filteredCommands.filter { it.startsWith(args[0].lowercase()) }
            }

            2 -> {
                if (!isOwner(sender)) {
                    return emptyList()
                }

                when (args[0].lowercase()) {
                    "grant", "revoke" -> Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }
}

private fun dev.wh2sperx.manager.MessageManager.send(
    sender: CommandSender,
    key: String,
    placeholders: Map<String, String> = emptyMap()
) {
    when (sender) {
        is org.bukkit.entity.Player -> send(sender, key, placeholders)
        else -> sendConsoleComponent(getMessage(key, placeholders))
    }
}