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
import org.bukkit.material.MaterialData
import uk.thinkofdeath.minecraft.alex.command.ArgumentParser
import uk.thinkofdeath.minecraft.alex.command.CommandRegistry
import uk.thinkofdeath.minecraft.alex.db.DB
import uk.thinkofdeath.minecraft.alex.db.blocks
import uk.thinkofdeath.minecraft.alex.db.gamemode

fun AlexPlugin.registerTypes(registry: CommandRegistry) {
    registry.addParser(javaClass<GameMode>(), RegistryParser(gamemode))
    registry.addParser(javaClass<Player>(), PlayerParser(this))
    registry.addParser(javaClass<World>(), WorldParser(this))
    registry.addParser(javaClass<MCTime>(), TimeParser())
    registry.addParser(javaClass<MaterialData>(), MaterialDataParser())
}

class RegistryParser<T>(val reg: DB<T>) : ArgumentParser<T> {

    override fun parse(argument: String): T {
        return reg[argument]
            ?: throw IllegalArgumentException("Unknown argument " + argument)
    }

    override fun complete(argument: String): Set<String> {
        val lower = argument.toLowerCase()
        val vals = hashSetOf<String>()
        for (v in reg.strToVal.keySet()) {
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

class TimeParser : ArgumentParser<MCTime> {
    override fun parse(argument: String): MCTime {
        return MCTime.fromString(argument)
    }

    override fun complete(argument: String): Set<String> = hashSetOf()

}

class MaterialDataParser : ArgumentParser<MaterialData> {
    override fun parse(argument: String): MaterialData {
        val low = argument.toLowerCase()
        return blocks[low] ?: throw IllegalArgumentException("Unknown block or item %s".format(argument))
    }

    override fun complete(argument: String): Set<String> {
        val low = argument.toLowerCase()
        val completions = hashSetOf<String>()
        for (v in blocks.strToVal.keySet()) {
            if (v.startsWith(low)) {
                completions.add(v)
            }
        }
        return completions
    }

}
