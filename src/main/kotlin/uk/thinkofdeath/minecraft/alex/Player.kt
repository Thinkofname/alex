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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bukkit.entity.Player
import java.io.File
import kotlin.properties.Delegates


class APlayer(val bukkit: Player, val plugin: AlexPlugin) {

    companion object {
        val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
    }

    private val dataFile = File(plugin.getDataFolder(), "players/%s.yml".format(bukkit.getUniqueId()))
    private val data: PlayerData

    init {
        if (dataFile.getParentFile() != null) {
            dataFile.getParentFile().mkdirs()
        }
        data = if (dataFile.exists()) {
            mapper.readValue(dataFile, javaClass<PlayerData>())
        } else {
            PlayerData()
        }

        data.lastName = bukkit.getName()
        if (data.displayName != null) {
            bukkit.setDisplayName(data.displayName)
        }
    }

    fun setDisplayName(name: String) {
        data.displayName = name
        bukkit.setDisplayName(name)
    }

    fun clearDisplayName() {
        bukkit.setDisplayName(bukkit.getName())
        data.displayName = null
    }

    fun remove() {
        mapper.writeValue(dataFile, data)
    }

    private data class PlayerData {
        var lastName: String by Delegates.notNull()
        var displayName: String? = null
    }
}

val Player.alex : APlayer
    get() = instance.playersUUID[getUniqueId()]
