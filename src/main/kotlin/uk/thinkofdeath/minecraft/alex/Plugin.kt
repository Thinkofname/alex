package uk.thinkofdeath.minecraft.alex

import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

class AlexPlugin : JavaPlugin {

    constructor() : super()

    // For debugging
    constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File)
    : super(loader, description, dataFolder, file)

}
