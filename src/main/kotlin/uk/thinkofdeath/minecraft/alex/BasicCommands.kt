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

import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.material.MaterialData
import uk.thinkofdeath.minecraft.alex.command.CommandHandler
import uk.thinkofdeath.minecraft.alex.db.blocks
import uk.thinkofdeath.minecraft.alex.hasPermission
import uk.thinkofdeath.minecraft.alex.command.cmd
import uk.thinkofdeath.minecraft.alex.command.cmds
import uk.thinkofdeath.minecraft.alex.command.doc

class BasicCommands(val plugin: AlexPlugin) : CommandHandler {

    // Gamemode commands

    cmds(
        cmd("gm ?"),
        cmd("gamemode ?")
    )
    hasPermission("alex.command.gamemode.self")
    doc("""
Changes your gamemode to the passed gamemode.
""")
    fun gamemode(sender: Player, gm: GameMode) {
        sender.setGameMode(gm)
        sender.sendMessage(
            "Gamemode changed to `%s`".format(gm.name().toLowerCase())
                .colorize()
        )
    }

    cmds(
        cmd("gm ? ?"),
        cmd("gm ?2 ?1"),
        cmd("gamemode ? ?"),
        cmd("gamemode ?2 ?1")
    )
    hasPermission("alex.command.gamemode.other")
    doc("""
Changes the gamemode of the player to the passed gamemode.
""")
    fun gamemode(sender: CommandSender, gm: GameMode, target: Player) {
        target.setGameMode(gm)
        sender.sendMessage("Gamemode changed to `%s` for `%s`".format(
            gm.name().toLowerCase(),
            target.getDisplayName()
        ).colorize())
        target.sendMessage("Your gamemode was changed to `%s`".format(
            gm.name().toLowerCase()
        ).colorize())
    }

    cmd("gms")
    hasPermission("alex.command.gamemode.self")
    doc("""
Changes your gamemode to survival mode.
""")
    fun gms(sender: Player) {
        gamemode(sender, GameMode.SURVIVAL)
    }

    cmd("gms ?")
    hasPermission("alex.command.gamemode.other")
    doc("""
Changes the gamemode of the player to survival mode.
""")
    fun gms(sender: CommandSender, target: Player) {
        gamemode(sender, GameMode.SURVIVAL, target)
    }

    cmd("gmc")
    hasPermission("alex.command.gamemode.self")
    doc("""
Changes your gamemode to creative mode.
""")
    fun gmc(sender: Player) {
        gamemode(sender, GameMode.CREATIVE)
    }

    cmd("gmc ?")
    hasPermission("alex.command.gamemode.other")
    doc("""
Changes the gamemode of the player to creative mode.
""")
    fun gmc(sender: CommandSender, target: Player) {
        gamemode(sender, GameMode.CREATIVE, target)
    }

    cmd("gma")
    hasPermission("alex.command.gamemode.self")
    doc("""
Changes your gamemode to adventure mode.
""")
    fun gma(sender: Player) {
        gamemode(sender, GameMode.ADVENTURE)
    }

    cmd("gma ?")
    hasPermission("alex.command.gamemode.other")
    doc("""
Changes the gamemode of the player to adventure mode.
""")
    fun gma(sender: CommandSender, target: Player) {
        gamemode(sender, GameMode.ADVENTURE, target)
    }

    cmd("gmsp")
    hasPermission("alex.command.gamemode.self")
    doc("""
Changes your gamemode to spectator mode.
""")
    fun gmsp(sender: Player) {
        gamemode(sender, GameMode.SPECTATOR)
    }

    cmd("gmsp ?")
    hasPermission("alex.command.gamemode.other")
    doc("""
Changes the gamemode of the player to spectator mode.
""")
    fun gmsp(sender: CommandSender, target: Player) {
        gamemode(sender, GameMode.SPECTATOR, target)
    }

    cmd("worlds")
    hasPermission("alex.command.world.list")
    doc("""
Returns a list of worlds on the server.
""")
    fun worlds(sender: CommandSender) {
        val worlds = sender.getServer().getWorlds()
        sender.sendMessage("Worlds(%d):".format(worlds.size()).colorize())
        for (world in worlds) {
            sender.sendMessage("- `%s`".format(world.getName()).colorize())
        }
    }

    cmd("time ?")
    hasPermission("alex.command.time")
    doc("""
Returns the current time of the world.
""")
    fun time(sender: CommandSender, world: World) {
        val time = world.getTime()
        sender.sendMessage("The current time in `%s` is `%s`".format(
            world.getName(),
            MCTime(time.toInt())
        ).colorize())
    }

    cmd("time")
    hasPermission("alex.command.time")
    doc("""
Returns the current time of the world you are in.
""")
    fun time(sender: Player) {
        time(sender, sender.getWorld())
    }

    cmd("time ? ?")
    hasPermission("alex.command.time.set")
    doc("""
Changes the time of the world.
""")
    fun time(sender: CommandSender, world: World, time: MCTime) {
        world.setTime(time.ticks.toLong())
        sender.sendMessage("Time set to `%s` in `%s`".format(
            time,
            world.getName()
        ).colorize())
    }

    cmd("time ?")
    hasPermission("alex.command.time.set")
    doc("""
Changes the time of the world you are currently in.
""")
    fun time(sender: Player, time: MCTime) {
        time(sender, sender.getWorld(), time)
    }

    cmds(
        cmd("i ?"),
        cmd("give ?")
    )
    hasPermission("alex.command.give")
    doc("""
Gives you a 64 stack of the named item.
""")
    fun give(sender: Player, mat: MaterialData) {
        give(sender, mat, 64)
    }

    cmds(
        cmd("i ? ?"),
        cmd("give ? ?")
    )
    hasPermission("alex.command.give")
    doc("""
Gives you a stack (with the passed size) of the named item.
""")
    fun give(sender: Player, mat: MaterialData, count: Int) {
        sender.getInventory().addItem(
            mat.toItemStack(count)
        )
        sender.sendMessage("Placed `%d` of `%s` in your inventory".format(
            count, blocks[mat]
        ).colorize())
    }

    cmd("nick")
    hasPermission("alex.command.nick")
    doc("""
Returns your current nickname.
""")
    fun nick(sender: Player) {
        sender.sendMessage("Your current nickname is `%s`".format(
            sender.getDisplayName()
        ).colorize())
    }

    cmd("nick ?")
    hasPermission("alex.command.nick.set")
    doc("""
Changes your current nickname.
""")
    fun nick(sender: Player, nick: String) {
        val pl = sender.alex
        val n = ChatColor.translateAlternateColorCodes('&', nick) + ChatColor.RESET
        pl.setDisplayName(n)
        sender.sendMessage("Your nickname has been changed to `%s`".format(
            n
        ).colorize())
    }

    cmd("nick clear")
    hasPermission("alex.command.nick.clear")
    doc("""
Removes your set nickname.
""")
    fun nickClear(sender: Player) {
        val pl = sender.alex
        pl.clearDisplayName()
        sender.sendMessage("Your nickname has been cleared".colorize())
    }

}

data class MCTime(val ticks: Int) {
    companion object {
        fun fromString(str: String): MCTime {
            if (':' !in str) {
                return MCTime(str.toInt())
            }
            val pos = str.indexOf(':')
            var hours = 24 + (str.substring(0, pos).toInt() - 6) % 24
            val minStr = str.substring(pos + 1).toLowerCase()
            val mins = if (minStr.endsWith("am")) {
                minStr.substring(0, minStr.length() - 2).toInt()
            } else if (minStr.endsWith("pm")) {
                hours += 12
                minStr.substring(0, minStr.length() - 2).toInt()
            } else {
                minStr.toInt()
            }
            return MCTime(hours * 1000 + ((mins.toDouble() / 60) * 1000).toInt())
        }
    }

    override public fun toString(): String {
        val hours = (6 + (ticks / 1000)) % 24
        val mins = (((ticks % 1000).toDouble() / 1000) * 60).toInt()
        return "%d:%02d".format(hours, mins)
    }
}
