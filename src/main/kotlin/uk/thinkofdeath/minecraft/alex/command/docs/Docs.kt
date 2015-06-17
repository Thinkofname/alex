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

package uk.thinkofdeath.minecraft.alex.command.docs

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import uk.thinkofdeath.minecraft.alex.*
import uk.thinkofdeath.minecraft.alex.command.*

class DocCommands(val plugin: AlexPlugin) : CommandHandler {

    cmd("ahelp")
    hasPermission("alex.command.help")
    doc("""
Displays `\/ahelp` usage information.
    """)
    fun help(sender: CommandSender) {
        sender.sendMessage("""
You can search for documentation for alex's commands
via the use of the `\/ahelp` command. Use
#\/ahelp < command without \/ >#
Replacing dynamic arguments (not sub commands) with
`*`. e.g:
#\/ahelp gamemode * *#
Incomplete commands will show all possible matches.
""".trim().colorize())
    }

    cmd("ahelp ?:search")
    hasPermission("alex.command.help")
    doc("""
Searches for documentation for passed search query.
Use `\/ahelp` for more information.
    """)
    fun help(sender: CommandSender, vararg search: String) {
        var s = search.joinToString(" ")
        val buf = StringBuilder()
        val nodes = plugin.registry.match(sender, s)
        for (node in nodes) {
            generateHelp(sender, buf, node)
        }
        if (buf.length() == 0) {
            sender.sendMessage("Couldn't find any matches for \"`$s`\"".error())
            return
        }
        sender.sendMessage("| Help for: `$s`".colorize())
        sender.sendMessage(buf.toString().colorize())
    }


    cmd("ahelp book")
    hasPermission("alex.command.help.book")
    doc("""
Creates a book containing all alex commands and
there documentation.
    """)
    fun help(sender: Player) {
        val book = ItemStack(Material.WRITTEN_BOOK, 1)
        val meta = book.getItemMeta() as BookMeta
        meta.setTitle("Alex Commands")
        meta.setAuthor("Thinkofdeath")

        fun String.bookColor(): String {
            return strColor(this, ChatColor.BLACK, ChatColor.DARK_AQUA)
        }

        val help = generateHelp(sender).bookColor()
        val pages = arrayListOf("""
Alex commands documentation

/Your guide to alex/
        """.trim().bookColor())

        var offset = 0
        while (true) {
            if (help.length() - offset < 256) {
                pages.add(help.substring(offset))
                break
            }
            pages.add(help.substring(offset, offset + 256))
            offset += 256
        }

        meta.setPages(pages)
        book.setItemMeta(meta)
        sender.getInventory().addItem(book)
    }

    fun generateHelp(caller: Any, node: Node = plugin.registry.root): String {
        val buf = StringBuilder()
        generateHelp(caller, buf, node)
        return buf.toString()
    }

    fun generateHelp(caller: Any, buf: StringBuilder, node: Node) {
        for (me in node.methods) {
            if (!me.key.isAssignableFrom(caller.javaClass)) {
                continue
            }
            try {
                for (t in me.value.validators) {
                    (t as ArgumentValidator<Any>).validate("", caller)
                }
            } catch (e: Exception) {
                continue
            }
            generateHelp(buf, me.value)
        }
        for (sub in node.subCommands.keySet().sort()) {
            val cmd = node.subCommands[sub]!!
            generateHelp(caller, buf, cmd)
        }
        for (arg in node.arguments) {
            generateHelp(caller, buf, arg.node)
        }
    }

    fun generateHelp(buf: StringBuilder, me: CMethod) {
        val args = me.desc.splitBy(" ")
        var argIndex = 1 // Skip the caller argument
        val params = me.method.getParameterTypes()
        buf.append("\\/")
        for (i in 0..args.size() - 1) {
            val arg = args[i]
            // Dynamic argument
            if (arg.startsWith("?")) {
                val pos = if (':' in arg) arg.indexOf(':') else arg.length()
                val index = if (arg == "?" || pos == 1) {
                    argIndex
                } else {
                    val id = arg.substring(1, pos).toInt()
                    id
                }

                val name = if (':' in arg)
                    arg.substring(pos+1)
                else params[index].getSimpleName().toLowerCase()

                buf.append("`<")
                buf.append(name)
                buf.append(">` ")

                argIndex++
            } else {
                buf.append("#")
                buf.append(arg)
                buf.append("# ")
            }
        }
        buf.append("\n")
        if (me.documentation.isEmpty()) {
            return
        }
        buf.append("- ")
        for (line in me.documentation.lines()) {
            if (line.isEmpty()) {
                buf.append("\n")
            } else {
                buf.append(line)
            }
        }
        buf.append("\n")
    }
}




