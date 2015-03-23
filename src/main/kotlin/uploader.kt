package org.jetbrains.intellij.pluginRepository.uploader

import org.jetbrains.intellij.pluginRepository.PluginRepositoryInstance
import java.io.File
import com.sampullara.cli.Argument
import com.sampullara.cli.Args

/**
 * @author nik
 */
private class CommandLineOptions {
    Argument(description = "Plugins repository host")
    var host = "http://plugins.jetbrains.com"

    Argument(description = "Plugin channel")
    var channel: String? = null

    Argument(required = true)
    var username: String? = null

    Argument(required = true)
    var password: String? = null

    Argument("plugin", required = true, description = "Plugin ID in the plugins repository")
    var pluginId: Int? = null

    Argument("file", required = true, description = "Path to plugin zip/jar file")
    var pluginPath: String? = null
}

fun main(args: Array<String>) {
    val options = CommandLineOptions()
    Args.parseOrExit(options, args)

    val pluginRepository = PluginRepositoryInstance(options.host, options.username!!, options.password!!)
    pluginRepository.uploadPlugin(options.pluginId!!, File(options.pluginPath!!), options.channel)
}
