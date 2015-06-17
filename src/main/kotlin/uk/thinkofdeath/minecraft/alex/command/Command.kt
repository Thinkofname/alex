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

import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Arrays
import java.util.Stack
import java.lang.reflect.Array as JArray

private val NO_ARG = Any()

class CommandRegistry {

    val parsers = hashMapOf<Class<*>, ArgumentParser<*>>()
    val root = Node()
    val splitter = "(?:`(.*?)`)|(?:(.*?)(\\s|$))".toPattern()

    init {
        addParser(javaClass<String>(), StringParser())
        addParser(javaClass<Int>(), IntParser())
    }

    fun <T>addParser(cls: Class<T>, parser: ArgumentParser<T>) {
        parsers[cls] = parser
    }

    fun register(handler: CommandHandler) {
        for (method in collectAnnotatedMethods(handler.javaClass)) {
            val single = method.getAnnotation(javaClass<cmd>())
            val commands = if (single == null) {
                method.getAnnotation(javaClass<cmds>()).value
            } else {
                arrayOf(single)
            }

            // Allow for private methods
            method.setAccessible(true)
            if (method.getParameterTypes().size() < 1) {
                throw RuntimeException("Must have a caller argument")
            }

            val methodArgs = method.getParameterTypes()
            val methodArgAnnotations = method.getParameterAnnotations()
            for (command in commands) {
                val args = command.value.splitBy(" ")
                var argIndex = 1 // Skip the caller argument
                val argPositions = IntArray(methodArgs.size())

                // This starts at the at the root node and
                // searches/creates branches until it reaches
                // its end where it places the method to be
                // called later

                var currentNode = root
                for (i in 0..args.size() - 1) {
                    val arg = args[i]
                    // Dynamic argument
                    if (arg.startsWith("?")) {
                        val pos = if (':' in arg) arg.indexOf(':') else arg.length()
                        val index = if (arg == "?" || pos == 1) {
                            argIndex
                        } else {
                            val id = arg.substring(1, pos).toInt()
                            if (id >= methodArgs.size() || id < 1) {
                                throw RuntimeException("Invalid explicit argument position")
                            }
                            id
                        }
                        if (argIndex >= methodArgs.size()) {
                            throw RuntimeException("Incorrect number of method paramters")
                        }
                        var argType = methodArgs[index]

                        val varargs = method.isVarArgs()
                            && methodArgs.size() == index + 1
                            && i == args.size() - 1
                        if (varargs) {
                            argType = argType.getComponentType()
                        }

                        if (argType !in parsers) {
                            throw RuntimeException("No parser for " + argType.getSimpleName())
                        }
                        val parser = parsers[argType]

                        // Obtain the annotations with argument validators and create
                        // instances of them using the annotation as the arguments
                        val annotations = methodArgAnnotations[index]
                        val argCheckers = processCommandAnnotations(argType, annotations)

                        val node = ANode(parser, argCheckers, if (varargs) argType else null)
                        currentNode.arguments.add(node)
                        // Branch into the node
                        currentNode = node.node
                        // Save the location of the argument
                        argPositions[argIndex] = index
                        argIndex++
                    } else {
                        // Constant
                        val a = arg.toLowerCase()
                        if (a !in currentNode.subCommands) {
                            currentNode.subCommands[a] = Node()
                        }
                        // Branch into the node
                        currentNode = currentNode.subCommands[a]!!
                    }
                }

                // Either we have a left over '?' or not enough
                if (argIndex != methodArgs.size()) {
                    throw RuntimeException("Incorrect number of method parameters")
                }

                // If we followed the route and got to a node with a method already then
                // another command has the same signature
                if (methodArgs[0] in currentNode.methods) {
                    throw RuntimeException("Duplicate command")
                }

                val av1 = processCommandAnnotations(methodArgs[0], methodArgAnnotations[0])
                val av2 = processCommandAnnotations(methodArgs[0], method.getAnnotations())
                val argumentValidators = Array(av1.size() + av2.size(), {
                    if (it < av1.size()) {
                        av1[it]
                    } else {
                        av2[it - av1.size()]
                    }
                })

                val docA = method.getAnnotation(javaClass<doc>())
                val documentation = if (docA == null) "" else docA.value

                currentNode.methods[methodArgs[0]] = CMethod(
                    command.value,
                    method,
                    handler,
                    argumentValidators,
                    argPositions,
                    documentation.trim()
                )
            }
        }
    }

    private fun collectAnnotatedMethods(of: Class<*>): List<Method> {
        val collected = arrayListOf<Method>()
        collectAnnotatedMethods(collected, of)
        return collected
    }

    private fun collectAnnotatedMethods(target: MutableList<Method>, of: Class<*>?) {
        // top of tree (parent of Object or parent of an interface)
        if (of == null) {
            return
        }

        outer@for (method in of.getDeclaredMethods()) {
            if (method.getAnnotation(javaClass<cmd>()) == null
                && method.getAnnotation(javaClass<cmds>()) == null) {
                continue
            }
            if (!Modifier.isPrivate(method.getModifiers())) {
                // check if the method is already defined
                for (other in target) {
                    if (other.getName() == method.getName()
                        && Arrays.equals(other.getParameterTypes(), method.getParameterTypes())) {
                        continue@outer
                    }
                }
            }
            target.add(method)
        }
    }

    private fun processCommandAnnotations(argType: Class<*>, annotations: Array<Annotation>): Array<ArgumentValidator<*>> {
        val validators = arrayListOf<ArgumentValidator<*>>()
        for (annotation in annotations) {
            val handler = annotation.annotationType().getAnnotation(javaClass<TypeHandler>())
                ?: continue
            if (!handler.clazz.isAssignableFrom(argType)) {
                throw RuntimeException(argType.getSimpleName() + " requires " + handler.clazz.javaClass.getSimpleName())
            }
            val constructor = handler.value
                .asSubclass(javaClass<ArgumentValidator<*>>())
                .getDeclaredConstructor(annotation.annotationType())
            constructor.setAccessible(true)
            validators.add(constructor.newInstance(annotation))

        }
        return validators.toTypedArray()
    }

    // TODO
    // This can most likely be cleaned up if we use
    // recursion instead. The performance hit should
    // be minimal

    fun execute(caller: Any, command: String) {
        // last error encounter whilst executing
        var lastError: Exception? = null
        val args = split(command)
        // Stores the states we can return if the current route fails
        val toTry = Stack<CommandState>()
        toTry.add(CommandState(root, caller, 0))
        // Try every possible route until we match a command or
        // run out of options
        while (!toTry.isEmpty()) {
            val state = toTry.pop()
            val currentNode = state.node
            var offset = state.offset
            if (offset == args.size()) {
                if (currentNode.methods.size() == 0) {
                    if (lastError == null) {
                        lastError = CommandException("Unknown command")
                    }
                    continue
                }
                for (method in currentNode.methods.values()) {
                    val type = method.method.getParameterTypes()[0];
                    if (type.isAssignableFrom(caller.javaClass)) {
                        try {
                            for (t in method.validators) {
                                (t as ArgumentValidator<Any>).validate("", caller)
                            }
                        } catch (e: Exception) {
                            lastError = e
                            continue
                        }

                        val arguments = arrayListOf<Any>()
                        var currentState: CommandState? = state;
                        while (currentState != null) {
                            if (currentState.argument != NO_ARG) {
                                arguments.add(currentState.argument)
                            }
                            currentState = currentState.parent
                        }

                        val processedArguments = Array(arguments.size(), { NO_ARG })
                        for (i in 0..processedArguments.size() - 1) {
                            processedArguments[method.positions[i]] = arguments[arguments.size() - 1 - i]
                        }
                        try {
                            method.method.invoke(method.owner, *processedArguments)
                        } catch (e: Exception) {
                            throw CommandException("Executing command", e)
                        }
                        return
                    } else {
                        lastError = CommandException("Incorrect caller")
                    }
                }
                continue
            }

            val arg = args[offset]
            val argLower = arg.toLowerCase()

            for (argumentNode in currentNode.arguments) {
                var out: Any?
                try {
                    val vtype = argumentNode.varargsType
                    if (vtype != null) {
                        out = JArray.newInstance(
                            vtype, args.size() - offset
                        )
                        for (i in offset..args.size() - 1) {
                            val parsed = argumentNode.parser.parse(args[i])
                            for (type in argumentNode.type) {
                                (type as ArgumentValidator<Any>).validate(args[i], parsed!!)
                            }
                            JArray.set(out, i - offset, parsed)
                        }
                        offset = args.size() - 1
                    } else {
                        out = argumentNode.parser.parse(arg)
                        for (type in argumentNode.type) {
                            (type as ArgumentValidator<Any>).validate(arg, out!!)
                        }
                    }
                } catch (e: Exception) {
                    lastError = e
                    continue
                }

                val newState = CommandState(argumentNode.node, out!!, offset + 1, state)
                toTry.add(newState)
            }
            // Check sub-commands
            if (argLower in currentNode.subCommands) {
                val next = currentNode.subCommands[argLower]!!
                val newState = CommandState(next, NO_ARG, offset + 1, state)
                toTry.add(newState)
            }
        }
        if (lastError != null) {
            if (lastError is CommandException) {
                throw lastError
            }
            throw CommandException("Failed to execute command", lastError)
        }
        throw CommandException("Unknown command")
    }

    fun complete(command: String): List<String> {
        val completions = hashSetOf<String>()
        val args = split(command)
        // Stores the states we can return if the current route fails
        val toTry = Stack<CommandState>()
        toTry.add(CommandState(root, NO_ARG, 0))
        // Try every possible route until we match a command or
        // run out of options
        while (!toTry.isEmpty()) {
            val state = toTry.pop()
            val currentNode = state.node
            var offset = state.offset

            val arg = args[offset]
            val argLower = arg.toLowerCase()

            if (offset == args.size() - 1) {
                for (sub in currentNode.subCommands.keySet()) {
                    if (sub.startsWith(argLower)) {
                        completions.add(sub)
                    }
                }
                for (node in currentNode.arguments) {
                    completions.addAll(node.parser.complete(arg))
                }
                continue
            }

            for (argumentNode in currentNode.arguments) {
                var out: Any?
                try {
                    val vtype = argumentNode.varargsType
                    if (vtype != null) {
                        out = JArray.newInstance(
                            vtype, args.size() - offset
                        )
                        for (i in offset..args.size() - 1) {
                            val parsed = argumentNode.parser.parse(args[i])
                            for (type in argumentNode.type) {
                                (type as ArgumentValidator<Any>).validate(args[i], parsed!!)
                            }
                            JArray.set(out, i - offset, parsed)
                        }
                        offset = args.size() - 1
                    } else {
                        out = argumentNode.parser.parse(arg)
                        for (type in argumentNode.type) {
                            (type as ArgumentValidator<Any>).validate(arg, out!!)
                        }
                    }
                } catch (e: Exception) {
                    continue
                }

                val newState = CommandState(argumentNode.node, out!!, offset + 1, state)
                toTry.add(newState)
            }
            // Check sub-commands
            if (argLower in currentNode.subCommands) {
                val next = currentNode.subCommands[argLower]!!
                val newState = CommandState(next, NO_ARG, offset + 1, state)
                toTry.add(newState)
            }
        }
        return completions.toList()
    }

    private fun split(command: String): List<String> {
        val m = splitter.matcher(command)
        val args = arrayListOf<String>()
        while (m.find()) {
            var arg = m.group().trim()
            if (arg.startsWith("`")) {
                arg = arg.substring(1, arg.length() - 1)
            }
            args.add(arg)
        }
        args.remove(args.size() - 1)
        return args
    }

    // Used for help searching
    fun match(caller: Any, command: String) : List<Node> {
        val args = split(command)
        // Stores the states we can return if the current route fails
        val toTry = Stack<CommandState>()
        toTry.add(CommandState(root, caller, 0))

        val nodes = arrayListOf<Node>()
        // Try every possible route until we match a command or
        // run out of options
        while (!toTry.isEmpty()) {
            val state = toTry.pop()
            val currentNode = state.node
            var offset = state.offset
            if (offset == args.size()) {
                nodes.add(state.node)
                continue
            }

            val arg = args[offset]
            val argLower = arg.toLowerCase()

            for (argumentNode in currentNode.arguments) {
                var out: Any? = null
                if (arg != "*") {
                    try {
                        val vtype = argumentNode.varargsType
                        if (vtype != null) {
                            out = JArray.newInstance(
                                vtype, args.size() - offset
                            )
                            for (i in offset..args.size() - 1) {
                                val parsed = argumentNode.parser.parse(args[i])
                                for (type in argumentNode.type) {
                                    (type as ArgumentValidator<Any>).validate(args[i], parsed!!)
                                }
                                JArray.set(out, i - offset, parsed)
                            }
                            offset = args.size() - 1
                        } else {
                            out = argumentNode.parser.parse(arg)
                            for (type in argumentNode.type) {
                                (type as ArgumentValidator<Any>).validate(arg, out!!)
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }

                val newState = CommandState(argumentNode.node, out ?: NO_ARG, offset + 1, state)
                toTry.add(newState)
            }
            // Check sub-commands
            if (argLower in currentNode.subCommands) {
                val next = currentNode.subCommands[argLower]!!
                val newState = CommandState(next, NO_ARG, offset + 1, state)
                toTry.add(newState)
            }
        }
        return nodes
    }

    // Make sure all the commands are registered
    fun checkCommands(plugin: JavaPlugin) {
        for (cmd in root.subCommands.keySet()) {
            val c = plugin.getCommand(cmd)
            if (c == null) {
                throw RuntimeException("Missing commands from plugin.yml \n\n" + generatePluginYaml())
            }
        }
    }

    fun generatePluginYaml(): String {
        return StringBuilder {
            append("commands:\n")
            for (cmd in root.subCommands.keySet().sort()) {
                append("  ")
                append(cmd)
                append(": {}\n")
            }
        }.toString()
    }
}

data class CommandState(
    val node: Node,
    val argument: Any,
    val offset: Int,
    val parent: CommandState? = null
)

class CommandException : Exception {
    constructor(cause: String) : super(cause)

    constructor(cause: String, ex: Exception) : super(cause, ex)
}
