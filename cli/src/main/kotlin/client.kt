package org.jetbrains.intellij.pluginRepository

import com.sampullara.cli.Args
import com.sampullara.cli.Argument
import java.io.File

class Client {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                System.err.println("Command is not specified: `upload` or `download` commands are supported.")
                System.exit(1)
            }
            val command = args[0]
            val restParameters = args.copyOfRange(1, args.size)
            if (command == "upload") {
                upload(restParameters)

            } else if (command == "download") {
                System.exit(if (download(restParameters) != null) 0 else 1)
            } else {
                System.err.println("Unknown command `$command`: `upload` or `download` commands are supported.")
                System.exit(1)
            }
        }

        private fun download(args: Array<String>): File? {
            val options = DownloadOptions()
            Args.parseOrExit(options, args)

            if (options.version.isNullOrBlank() && options.ideBuild.isNullOrBlank()) {
                System.err.print("`version` or `ide-build` must be specified")
                System.exit(1)
            }

            val pluginRepository = PluginRepositoryInstance(options.host, null, null)
            val channel = parseChannel(options.channel)
            if (!options.version.isNullOrBlank()) {
                return pluginRepository.download(options.pluginId!!, options.version!!, channel, options.destination)
            } else {
                return pluginRepository.downloadCompatiblePlugin(options.pluginId!!, options.ideBuild!!, channel,
                        options.destination)
            }

        }

        private fun upload(args: Array<String>) {
            val options = UploadOptions()
            Args.parseOrExit(options, args)
            val pluginRepository = PluginRepositoryInstance(options.host, options.username!!, options.password!!)
            val pluginId = options.pluginId!!
            if (pluginId.matches(Regex("\\d+"))) {
                pluginRepository.uploadPlugin(pluginId.toInt(), File(options.pluginPath!!), parseChannel(options.channel))
            } else {
                pluginRepository.uploadPlugin(pluginId, File(options.pluginPath!!), parseChannel(options.channel))
            }
        }

        private fun parseChannel(channel: String?) = if (!channel.isNullOrEmpty() && channel != "_default_") channel else null
    }

    class UploadOptions : BaseOptions() {
        @set:Argument("plugin", required = true, description = "Plugin ID in the plugins repository or ID defined in plugin.xml")
        var pluginId: String? = null

        @set:Argument(required = true)
        var username: String? = null

        @set:Argument(required = true)
        var password: String? = null

        @set:Argument("file", required = true, description = "Path to plugin zip/jar file")
        var pluginPath: String? = null
    }

    class DownloadOptions : BaseOptions() {
        @set:Argument("plugin", required = true, description = "Plugin ID defined in plugin.xml")
        var pluginId: String? = null

        @set:Argument("version", description = "Plugin version to download")
        var version: String? = null

        @set:Argument("ide-build", description = "IDE build number with product code to download plugin compatible with (e.g. IC-145.184)")
        var ideBuild: String? = null

        @set:Argument("to", description = "Target filepath")
        var destination: String = "."
    }

    open class BaseOptions {
        @set:Argument(description = "Plugins repository host")
        var host = "http://plugins.jetbrains.com"

        @set:Argument(description = "Plugin channel")
        var channel: String? = null
    }
}
