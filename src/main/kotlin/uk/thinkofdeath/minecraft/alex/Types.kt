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
import org.bukkit.entity.Player
import uk.thinkofdeath.minecraft.alex.command.ArgumentParser
import uk.thinkofdeath.minecraft.alex.command.CommandRegistry

fun AlexPlugin.registerTypes(registry: CommandRegistry) {
    registry.addParser(javaClass<GameMode>(), GameModeParser())
    registry.addParser(javaClass<Player>(), PlayerParser(this))
    registry.addParser(javaClass<World>(), WorldParser(this))
}

class GameModeParser : ArgumentParser<GameMode> {

    val matches = mapOf(
        "s" to GameMode.SURVIVAL,
        "0" to GameMode.SURVIVAL,
        "survival" to GameMode.SURVIVAL,

        "c" to GameMode.CREATIVE,
        "1" to GameMode.CREATIVE,
        "creative" to GameMode.CREATIVE,

        "a" to GameMode.ADVENTURE,
        "2" to GameMode.ADVENTURE,
        "adventure" to GameMode.ADVENTURE,

        "sp" to GameMode.SPECTATOR,
        "3" to GameMode.SPECTATOR,
        "spectator" to GameMode.SPECTATOR
    )

    override fun parse(argument: String): GameMode {
        val lower = argument.toLowerCase()
        if (lower in matches) {
            return matches[lower]!!
        }
        throw IllegalArgumentException("Unknown gamemode " + argument)
    }

    override fun complete(argument: String): Set<String> {
        val lower = argument.toLowerCase()
        val vals = hashSetOf<String>()
        for (v in matches.keySet()) {
            if (v.startsWith(lower)) {
                vals.add(v)
            }
        }
        return vals
    }
}

class PlayerParser(val plugin: AlexPlugin) : ArgumentParser<Player> {
    override fun parse(argument: String): Player {
        val player = plugin.getServer().getPlayer(argument)
            ?: throw IllegalArgumentException("No player named %s is online".format(argument))
        return player
    }

    override fun complete(argument: String): Set<String> {
        val completions = hashSetOf<String>()
        for (player in plugin.getServer().matchPlayer(argument)) {
            completions.add(player.getName())
        }
        return completions
    }

}

class WorldParser(val plugin: AlexPlugin) : ArgumentParser<World> {
    override fun parse(argument: String): World {
        return plugin.getServer().getWorld(argument)
            ?: throw IllegalArgumentException("No world named %s".format(argument))
    }

    override fun complete(argument: String): Set<String> {
        val completions = hashSetOf<String>()
        val low = argument.toLowerCase()
        for (world in plugin.getServer().getWorlds()) {
            if (world.getName().toLowerCase().startsWith(low)) {
                completions.add(world.getName())
            }
        }
        return completions
    }

}
