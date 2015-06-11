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

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.material.MaterialData
import org.bukkit.material.Wool


inline fun DB<org.bukkit.material.MaterialData>.reg(m: Material, d: Byte = 0, body: DB.RegBody.() -> Unit) {
    reg(MaterialData(m, d), body)
}

val blocks = registry<MaterialData> {
    // Defaults based on bukkit names
    for (m in Material.values()) {
        if (m.isBlock()) {
            val mat = MaterialData(m, 0)
            strToVal[m.name().toLowerCase().replace("_", "")] = mat
            valToStr[mat] = m.name().toNormalCase()
        }
    }

    reg(Wool(DyeColor.WHITE)) {
        +"Cloth"
        +"White Cloth"
        +"White Wool"
        +"Wool"
    }
    reg(Wool(DyeColor.ORANGE)) {
        +"Orange Cloth"
        +"Orange Wool"
    }
    reg(Wool(DyeColor.MAGENTA)) {
        +"Magenta Cloth"
        +"Magenta Wool"
    }
    reg(Wool(DyeColor.LIGHT_BLUE)) {
        +"Light Blue Cloth"
        +"Light Blue Wool"
    }
    reg(Wool(DyeColor.YELLOW)) {
        +"Yellow Cloth"
        +"Yellow Wool"
    }
    reg(Wool(DyeColor.LIME)) {
        +"Lime Cloth"
        +"Lime Wool"
    }
    reg(Wool(DyeColor.PINK)) {
        +"Pink Cloth"
        +"Pink Wool"
    }
    reg(Wool(DyeColor.GRAY)) {
        +"Grey Cloth"
        +"Gray Cloth"
        +"Grey Wool"
        +"Gray Wool"
    }
    reg(Wool(DyeColor.SILVER)) {
        +"Light Grey Wool"
        +"Light Grey Cloth"
        +"Silver Wool"
        +"Silver Cloth"
        +"Light Gray Cloth"
        +"Light Gray Wool"
    }
    reg(Wool(DyeColor.CYAN)) {
        +"Cyan Cloth"
        +"Cyan Wool"
    }
    reg(Wool(DyeColor.PURPLE)) {
        +"Purple Cloth"
        +"Purple Wool"
    }
    reg(Wool(DyeColor.BROWN)) {
        +"Brown Cloth"
        +"Brown Wool"
    }
    reg(Wool(DyeColor.GREEN)) {
        +"Green Cloth"
        +"Green Wool"
    }
    reg(Wool(DyeColor.RED)) {
        +"Red Cloth"
        +"Red Wool"
    }
    reg(Wool(DyeColor.BLACK)) {
        +"Black Cloth"
        +"Black Wool"
    }
}

