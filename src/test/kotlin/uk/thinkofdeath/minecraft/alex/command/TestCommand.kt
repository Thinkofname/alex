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

import kotlin.test.assertEquals
import kotlin.test.fail
import org.junit.Test as test
import uk.thinkofdeath.minecraft.alex.command.cmd as command

class CommandTest {
    test fun registerBasic() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("helloworld")
            fun helloWorld(sender: String) {

            }

            command("command2")
            fun myCommand(sender: String) {

            }

            // Different sender
            command("helloworld")
            fun helloWorld(sender: Int) {

            }
        })
    }

    test fun registerComplex() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("this command has a lot of sub commands")
            fun subCommands(sender: String) {

            }

            command("tell ? ?")
            fun tell(sender: String, target: String, message: String) {

            }

            command("addandtell ? ? ?")
            fun addTell(sender: String, a: Int, b: Int, target: String) {

            }
        })
    }

    test(expected = Exception::class)
    fun registerUnparsable() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("hello ?")
            fun nope(sender: String, o: Any) {
            }
        })
    }

    test(expected = Exception::class)
    fun registerIncorrectArgCount() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("hello ?")
            fun nope(sender: String) {
            }
        })
    }

    test(expected = Exception::class)
    fun registerIncorrectArgCount2() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("hello")
            fun nope(sender: String, arg: Int) {
            }
        })
    }

    test fun executeSingle() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            var call = 0

            command("first")
            fun first(sender: String) {
                assertEquals("Jim", sender)
                assertEquals(0, call++)
            }

            command("second")
            fun second(sender: Integer) {
                assertEquals(5, sender)
                assertEquals(1, call++)
            }

            command("third")
            fun third(sender: String) {
                assertEquals("Jimmy", sender)
                assertEquals(2, call++)
            }
        })
        reg.execute("Jim", "first")
        reg.execute(5, "second")
        reg.execute("Jimmy", "third")
    }

    test fun executeComplex() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            var call = 0

            command("test sub commands one")
            fun subCommands(sender: String) {
                assertEquals("bob", sender);
                assertEquals(0, call++);
            }

            command("test sub commands two")
            fun subCommands2(sender: String) {
                assertEquals("bob", sender);
                assertEquals(1, call++);
            }

            command("give ? ?")
            fun arguments(sender: String, name: String, money: Int) {
                assertEquals("jimmy", sender);
                assertEquals(2, call++);
                assertEquals("timmy", name);
                assertEquals(55, money);
            }

            command("give ? ~ ?")
            fun argumentsWithSub(sender: String, name: String, money: Int) {
                assertEquals("jimmy", sender);
                assertEquals(3, call++);
                assertEquals("timmy", name);
                assertEquals(55, money);
            }
        })

        reg.execute("bob", "test sub commands one");
        reg.execute("bob", "test sub commands two");
        reg.execute("jimmy", "give timmy 55");
        reg.execute("jimmy", "give timmy ~ 55");
    }

    test(expected = CommandException::class)
    fun noCommand() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
        })
        reg.execute("hello", "world")
    }

    test(expected = CommandException::class)
    fun noSubCommand() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("world create")
            fun test(sender: String) {
                fail("Shouldn't be called")
            }
        })
        reg.execute("hello", "world remove")
    }

    test(expected = CommandException::class)
    fun noSubCommand2() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("world ? create")
            fun test(sender: String, name: String) {
                fail("Shouldn't be called")
            }
        })
        reg.execute("hello", "world testing remove")
    }

    test
    fun differentCaller() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("hello")
            fun call(sender: String) {
            }

            command("hello")
            fun call(sender: Integer) {
            }
        })
        reg.execute("hey", "hello")
        reg.execute(5, "hello")
    }

    test
    fun explicitPosition() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("test ?2 ?3 ?1")
            fun test(sender: String, a: String, b: String, c: String) {
                assertEquals("a", b)
                assertEquals("b", c)
                assertEquals("c", a)
            }
        })
        reg.execute("tester", "test a b c")
    }

    test(expected = CommandException::class)
    fun limit() {
        val reg = CommandRegistry()
        reg.register(object : CommandHandler {
            command("test ?")
            fun test(sender: String, MaxLength(3) arg: String) {
                fail()
            }
        })
        reg.execute("tester", "test aaaa")
    }

    test fun executeMultipleBasic() {
        val reg = CommandRegistry()
        var callCount = 0
        reg.register(object : CommandHandler {
            cmds(
                    command("a"),
                    command("b")
            )
            fun test(sender: String) {
                callCount++
            }
        })
        reg.execute("", "a")
        reg.execute("", "b")
        assertEquals(2, callCount)
    }

    test fun executeMultipleComplex() {
        val reg = CommandRegistry()
        var callCount = 0
        reg.register(object : CommandHandler {
            cmds(
                    command("a ?"),
                    command("b ?")
            )
            fun test(sender: String, arg: Int) {
                callCount++
                assertEquals(callCount, arg)
            }
        })
        reg.execute("", "a 1")
        reg.execute("", "b 2")
        assertEquals(2, callCount)
    }
}
