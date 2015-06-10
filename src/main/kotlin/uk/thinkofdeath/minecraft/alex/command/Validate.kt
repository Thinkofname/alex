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

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

interface ArgumentValidator<T> {
    fun validate(argString: String, arg: T)
}

Target(ElementType.ANNOTATION_TYPE)
Retention(RetentionPolicy.RUNTIME)
annotation class TypeHandler(val value: Class<out ArgumentValidator<*>>, val clazz: Class<*>)


Target(ElementType.PARAMETER, ElementType.METHOD)
Retention(RetentionPolicy.RUNTIME)
TypeHandler(value = javaClass<MaxLengthHandler>(), clazz = javaClass<String>())
annotation class MaxLength(val value: Int)

class MaxLengthHandler : ArgumentValidator<String> {

    val max: Int

    constructor(maxLength: MaxLength) {
        max = maxLength.value
    }

    override fun validate(argString: String, arg: String) {
        if (arg.length() > max) {
            throw IllegalArgumentException("%s is too long (%d > %d)".format(arg, arg.length(), max))
        }
    }

}
