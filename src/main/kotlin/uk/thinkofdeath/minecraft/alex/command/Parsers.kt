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

interface ArgumentParser<T> {

    fun parse(argument: String): T

    fun complete(argument: String): Set<String>
}

class StringParser : ArgumentParser<String> {
    override fun parse(argument: String): String {
        return argument
    }

    override fun complete(argument: String): Set<String> {
        return hashSetOf()
    }

}

class IntParser : ArgumentParser<Int> {
    override fun parse(argument: String): Int {
        return argument.toInt()
    }

    override fun complete(argument: String): Set<String> {
        return hashSetOf()
    }

}