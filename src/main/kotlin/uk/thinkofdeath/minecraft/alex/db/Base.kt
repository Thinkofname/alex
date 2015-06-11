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

package uk.thinkofdeath.minecraft.alex.db

class DB<T> {
    val strToVal = hashMapOf<String, T>()
    val valToStr = hashMapOf<T, String>()

    fun get(k: String): T? {
        return strToVal[k.toLowerCase().replace(" ", "")]
    }
    fun get(k: T): String? {
        return valToStr[k]
    }

    inline fun reg(v: T, body: RegBody.() -> Unit) {
        val r = RegBody(v)
        r.body()
    }

    inner class RegBody(val v: T) {
        fun String.plus() {
            strToVal[this.toLowerCase().replace(" ", "")] = v
            valToStr[v] = this
        }
    }
}

fun registry<T>(body: DB<T>.() -> Unit): DB<T> {
    val db = DB<T>()
    db.body()
    return db
}

fun String.toNormalCase(): String {
    val builder = StringBuilder()
    var nextCap = true
    for (c in this) {
        if (c == '_') {
            nextCap = true
            builder.append(' ')
        } else if (nextCap) {
            builder.append(Character.toUpperCase(c))
            nextCap = false
        } else {
            builder.append(Character.toLowerCase(c))
        }
    }
    return builder.toString()
}
