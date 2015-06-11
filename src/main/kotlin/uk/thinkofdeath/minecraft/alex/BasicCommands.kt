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

import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import uk.thinkofdeath.minecraft.alex.command.CommandHandler
import uk.thinkofdeath.minecraft.alex.HasPermission as hasPermission
import uk.thinkofdeath.minecraft.alex.command.Command as cmd
import uk.thinkofdeath.minecraft.alex.command.Commands as cmds

class BasicCommands(val plugin: AlexPlugin) : CommandHandler {

    cmds(
        cmd("gm ?"),
        cmd("gamemode ?")
    )
    hasPermission("alex.command.gamemode.self")
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

    cmd("worlds")
    hasPermission("alex.command.world.list")
    fun worlds(sender: CommandSender) {
        val worlds = sender.getServer().getWorlds()
        sender.sendMessage("Worlds(%d):".format(worlds.size()).colorize())
        for (world in worlds) {
            sender.sendMessage("- `%s`".format(world.getName()).colorize())
        }
    }

    cmd("time ?")
    hasPermission("alex.command.time")
    fun time(sender: CommandSender, world: World) {
        val time = world.getTime()
        sender.sendMessage("The current time in `%s` is `%s`".format(
            world.getName(),
            formatMinecraftTime(time.toInt())
        ).colorize())
    }

    cmd("time")
    hasPermission("alex.command.time")
    fun time(sender: Player) {
        time(sender, sender.getWorld())
    }

    cmd("time ? ?")
    hasPermission("alex.command.time.set")
    fun time(sender: CommandSender, world: World, time: Int) {
        world.setTime(time.toLong())
        sender.sendMessage("Time set to `%s` in `%s`".format(
            formatMinecraftTime(time),
            world.getName()
        ).colorize())
    }

    cmd("time ?")
    hasPermission("alex.command.time.set")
    fun time(sender: Player, time: Int) {
        time(sender, sender.getWorld(), time)
    }

}

fun formatMinecraftTime(time: Int) : String{
    val hours = (6 + (time / 1000)) % 24
    val mins = (((time % 1000).toDouble() / 1000) * 60).toInt()
    return "%d:%02d".format(hours, mins)
}
