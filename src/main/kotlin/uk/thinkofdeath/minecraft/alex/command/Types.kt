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

package uk.thinkofdeath.minecraft.alex.command

import java.lang.annotation.*
import java.lang.reflect.Method

Target(ElementType.METHOD)
Retention(RetentionPolicy.RUNTIME)
Repeatable(Commands::class)
annotation class Command(val value: String)

Target(ElementType.METHOD)
Retention(RetentionPolicy.RUNTIME)
annotation class Commands(vararg val value: Command)

interface CommandHandler

data class Node(
    val subCommands: MutableMap<String, Node> = hashMapOf(),
    val arguments: MutableList<ANode> = arrayListOf(),
    val methods: MutableMap<Class<*>, CMethod> = hashMapOf()
)

data class CMethod(
    val method: Method,
    val owner: CommandHandler,
    val validators: Array<ArgumentValidator<*>>,
    val positions: Array<Int>
)

data class ANode(
    val parser: ArgumentParser<*>,
    val type: Array<ArgumentValidator<*>>,
    val varargsType: Class<*>?,
    val node: Node = Node()
)
