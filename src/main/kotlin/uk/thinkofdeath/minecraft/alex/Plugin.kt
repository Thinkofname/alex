/*
 * Copyright 2015 Matthew Collins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.thinkofdeath.minecraft.alex

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import uk.thinkofdeath.minecraft.alex.command.CommandException
import uk.thinkofdeath.minecraft.alex.command.CommandRegistry
import java.io.File
import java.util.*

val instance : AlexPlugin
    get() = Bukkit.getPluginManager().getPlugin("Alex") as AlexPlugin

class AlexPlugin : JavaPlugin {

    val registry = CommandRegistry()
    val playersUUID = hashMapOf<UUID, APlayer>()
    val playersName = hashMapOf<String, APlayer>()
    val events = Events(this)

    constructor() : super()

    // For debugging
    constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File)
    : super(loader, description, dataFolder, file)

    override fun onEnable() {
        registerTypes(registry)
        registry.register(BasicCommands(this))
        getServer().getPluginManager().registerEvents(events, this)
        for (player in getServer().getOnlinePlayers()) {
            events.on(PlayerJoinEvent(player, ""))
        }

        registry.checkCommands(this)
    }

    override fun onDisable() {
        for (player in getServer().getOnlinePlayers()) {
            events.on(PlayerQuitEvent(player, ""))
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val cmd = StringBuilder(command.getName())
        for (a in args) {
            cmd.append(' ').append(a)
        }
        try {
            registry.execute(sender, cmd.toString())
        } catch (e: CommandException) {
            sender.sendMessage(("Error: `" + e.getMessage() + "`").error())
            if (e.getCause() != null) {
                sender.sendMessage(e.getCause()?.getMessage()?.error())
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        val cmd = StringBuilder(command.getName())
        for (a in args) {
            cmd.append(' ').append(a)
        }
        return registry.complete(cmd.toString())
    }

}
