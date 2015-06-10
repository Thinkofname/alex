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
import org.bukkit.entity.Player
import uk.thinkofdeath.minecraft.alex.command.CommandHandler
import uk.thinkofdeath.minecraft.alex.command.Command as cmd
import uk.thinkofdeath.minecraft.alex.command.Commands as cmds
import uk.thinkofdeath.minecraft.alex.HasPermission as hasPermission

class BasicCommands : CommandHandler {


    cmds(
            cmd("gm ?"),
            cmd("gamemode ?")
    )
    hasPermission("alex.command.gamemode.self")
    fun gamemode(sender: Player, gm: GameMode) {
        sender.setGameMode(gm)
        sender.sendMessage("Gamemode changed to %s".format(gm.name().toLowerCase()))
    }
}