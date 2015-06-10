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

import org.bukkit.command.CommandSender
import uk.thinkofdeath.minecraft.alex.command.ArgumentValidator
import uk.thinkofdeath.minecraft.alex.command.TypeHandler
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target


Target(ElementType.PARAMETER, ElementType.METHOD)
Retention(RetentionPolicy.RUNTIME)
TypeHandler(value = javaClass<HasPermissionHandler>(), clazz = javaClass<CommandSender>())
annotation class HasPermission(vararg val value: String, val wildcard : Boolean = true)

class HasPermissionHandler : ArgumentValidator<CommandSender> {

    val permissions : Array<out String>
    val wildcard : Boolean

    constructor(hasPermission: HasPermission) {
        permissions = hasPermission.value
        wildcard = hasPermission.wildcard
    }

    override fun validate(argString: String, arg: CommandSender) {
        for (permission in permissions) {
            // Check for the permission
            if (arg.hasPermission(permission)) {
                return;
            }
            // If the permission was manually set to false
            // don't bother with the wildcards
            if (!arg.isPermissionSet(permission) && wildcard) {
                var perm = permission;
                while (perm.indexOf('.') != -1) {
                    perm = perm.substring(0, perm.lastIndexOf('.'));
                    if (arg.isPermissionSet(perm + ".*")) {
                        if (arg.hasPermission(perm + ".*")) {
                            return;
                        } else {
                            throw IllegalArgumentException("No permission for command");
                        }
                    }
                }
            }
        }
        throw IllegalArgumentException("No permission for command");
    }

}