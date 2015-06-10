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

// Formats the string in our standard color scheme
//     ` Highlights the section
//     # Bolds the section
//     / Italics the section
fun String.colorize(): String {
    return strColor(this, ChatColor.AQUA, ChatColor.DARK_AQUA)
}

// See colorize
fun String.error(): String {
    return strColor(this, ChatColor.RED, ChatColor.DARK_RED)
}

fun strColor(str: String, light: ChatColor, dark: ChatColor): String {
    val builder = StringBuilder(light.toString())
    var isFormatting = false
    for (c in str) {
        when (c) {
            '`' -> if (isFormatting) {
                isFormatting = false
                builder.append(light)
            } else {
                isFormatting = true
                builder.append(dark)
            }
            '#' -> if (isFormatting) {
                isFormatting = false
                builder.append(light)
            } else {
                isFormatting = true
                builder.append(ChatColor.BOLD)
            }
            '/' -> if (isFormatting) {
                isFormatting = false
                builder.append(light)
            } else {
                isFormatting = true
                builder.append(ChatColor.ITALIC)
            }
            else -> builder.append(c)
        }
    }
    return builder.toString()
}
