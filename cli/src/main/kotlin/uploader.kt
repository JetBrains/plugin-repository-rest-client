package org.jetbrains.intellij.pluginRepository

import java.io.File
import com.sampullara.cli.Argument
import com.sampullara.cli.Args
import kotlin.platform.platformStatic

/**
 * @author nik
 */
public class Uploader {
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

    companion object {
        platformStatic
        fun main(args: Array<String>) {
            val options = Uploader()
            Args.parseOrExit(options, args)

            val pluginRepository = PluginRepositoryInstance(options.host, options.username!!, options.password!!)
            val channel = if (options.channel.isNotEmpty() && options.channel != "_default_") options.channel else null
            pluginRepository.uploadPlugin(options.pluginId!!, File(options.pluginPath!!), channel)
        }
    }
}
