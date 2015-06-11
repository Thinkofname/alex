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

import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.EventHandler as event

class Events(val plugin: AlexPlugin) : Listener{

    event fun on(e : PlayerJoinEvent) {
        val pl = APlayer(e.getPlayer(), plugin)
        plugin.playersName[e.getPlayer().getName()] = pl
        plugin.playersUUID[e.getPlayer().getUniqueId()] = pl
    }

    event fun on(e : PlayerQuitEvent) {
        var pl : APlayer? = plugin.playersName.remove(e.getPlayer().getName())
        pl = plugin.playersUUID.remove(e.getPlayer().getUniqueId()) ?: pl
        pl?.remove()
    }
}
