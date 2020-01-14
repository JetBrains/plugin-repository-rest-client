package org.jetbrains.intellij.pluginRepository

import com.sampullara.cli.Args
import com.sampullara.cli.Argument
import java.io.File
import kotlin.system.exitProcess

class Client {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                System.err.println("Command is not specified: `upload`, `download`, `list` or `info` commands are supported.")
                exitProcess(1)
            }
            val command = args[0]
            val restParameters = args.copyOfRange(1, args.size)
            when (command) {
                "upload" -> upload(restParameters)
                "download" -> exitProcess(if (download(restParameters) != null) 0 else 1)
                "list" -> list(restParameters)
                "info" -> info(restParameters)
                else -> {
                    System.err.println("Unknown command `$command`: `upload`, `download`, `list` or `info` commands are supported.")
                    exitProcess(1)
                }
            }
        }

        private fun download(args: Array<String>): File? {
            val options = DownloadOptions()
            Args.parseOrExit(options, args)

            if (options.version.isNullOrBlank() && options.ideBuild.isNullOrBlank()) {
                System.err.print("`version` or `ide-build` must be specified")
                exitProcess(1)
            }

            val pluginRepository = PluginRepositoryInstance(options.host)
            val channel = parseChannel(options.channel)
            return if (!options.version.isNullOrBlank()) {
                pluginRepository.download(options.pluginId!!, options.version!!, channel, File(options.destination))
            } else {
                pluginRepository.downloadCompatiblePlugin(options.pluginId!!, options.ideBuild!!, channel,
                        File(options.destination))
            }

        }

        private fun upload(args: Array<String>) {
            val options = UploadOptions()
            Args.parseOrExit(options, args)
            val pluginRepository = PluginRepositoryInstance(options.host, options.token)
            val pluginId = options.pluginId
            when {
                pluginId == null -> pluginRepository.uploadNewPlugin(File(options.pluginPath!!), options.family!!, 104, "https://plugins.jetbrains.com/legal/terms-of-use")
                pluginId.matches(Regex("\\d+")) -> pluginRepository.uploadPlugin(pluginId.toInt(), File(options.pluginPath!!), parseChannel(options.channel), options.notes)
                else -> pluginRepository.uploadPlugin(pluginId, File(options.pluginPath!!), parseChannel(options.channel), options.notes)
            }
        }

        private fun list(args: Array<String>) {
            val options = ListOptions()
            Args.parseOrExit(options, args)
            val pluginRepository = PluginRepositoryInstance(options.host)
            val channel = parseChannel(options.channel)
            val plugins = pluginRepository.listPlugins(options.ideBuild!!, channel, options.pluginId)
            for (plugin in plugins) {
                println("${plugin.name} (${plugin.id} version ${plugin.version}, IDE builds ${plugin.sinceBuild} to ${plugin.untilBuild}, depends ${plugin.depends}")
            }
        }

        private fun info(args: Array<String>) {
            val options = InfoOptions()
            Args.parseOrExit(options, args)
            val pluginRepository = PluginRepositoryInstance(options.host)
            val plugin = pluginRepository.pluginInfo(options.family!!, options.pluginId!!)
            if (plugin != null) {
                println("${plugin.name} ${plugin.id} made by ${plugin.vendor?.name}")
            }
        }

        private fun parseChannel(channel: String?) = if (!channel.isNullOrEmpty() && channel != "_default_") channel else null
    }

    class UploadOptions : BaseOptions() {
        @set:Argument("plugin", required = false, description = "Plugin ID in the plugins repository or ID defined in plugin.xml")
        var pluginId: String? = null

        @set:Argument(required = true, description = "Hub permanent token")
        var token: String? = null

        @set:Argument("file", required = true, description = "Path to plugin zip/jar file")
        var pluginPath: String? = null

        @set:Argument("family", description = "Plugin's family")
        var family: String? = "intellij"

        @set:Argument(description = "Change notes (may include HTML tags). The argument is ignored when uploading updates for IntelliJ-based IDEs")
        var notes: String? = null
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

    class ListOptions : BaseOptions() {
        @set:Argument("ide-build", required = true, description = "IDE build number with product code to list plugins compatible with (e.g. IC-145.184)")
        var ideBuild: String? = null

        @set:Argument("plugin", description = "Plugin ID defined in plugin.xml")
        var pluginId: String? = null
    }

    class InfoOptions : BaseOptions() {
        @set:Argument("plugin", required = true, description = "Plugin ID defined in plugin.xml")
        var pluginId: String? = null

        @set:Argument("family", description = "Plugin's family")
        var family: String? = "intellij"
    }

    open class BaseOptions {
        @set:Argument(description = "Plugins repository host")
        var host = "https://plugins.jetbrains.com"

        @set:Argument(description = "Plugin channel")
        var channel: String? = null
    }
}
