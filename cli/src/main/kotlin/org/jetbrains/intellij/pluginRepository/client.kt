package org.jetbrains.intellij.pluginRepository

import com.sampullara.cli.Args
import com.sampullara.cli.Argument
import org.jetbrains.intellij.pluginRepository.model.*
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

      val pluginRepository = PluginRepositoryFactory.create(
        options.host).downloader
      val channel = parseChannel(options.channel)
      return if (!options.version.isNullOrBlank()) {
        if (options.oldFile.isBlank()) {
          pluginRepository.download(options.pluginId!!, options.version!!, File(options.destination), channel)
        } else {
          pluginRepository.downloadViaBlockMap(options.pluginId!!, options.version!!, File(options.destination), File(options.oldFile), channel)
        }
      } else {
        if (options.oldFile.isBlank()) {
          pluginRepository.downloadLatestCompatiblePlugin(options.pluginId!!, options.ideBuild!!, File(options.destination),
            channel)
        } else {
          pluginRepository.downloadLatestCompatiblePluginViaBlockMap(options.pluginId!!, options.ideBuild!!, File(options.destination),
            File(options.oldFile), channel)
        }
      }

    }

    private fun upload(args: Array<String>) {
      val options = UploadOptions()
      Args.parseOrExit(options, args)
      val pluginRepository = PluginRepositoryFactory.create(
        options.host, options.token).uploader
      val pluginId = options.pluginId
      when {
        pluginId == null -> uploadNewPlugin(pluginRepository, options)
        else -> uploadPluginUpdate(pluginRepository, options)
      }
    }

    private fun uploadNewPlugin(uploader: PluginUploader, options: UploadOptions): PluginBean {
      println("New plugin upload has started.")
      val family = options.family

      if (family == null) {
        System.err.print("`family` must be specified for new plugin upload")
        exitProcess(1)
      }

      return uploader.uploadNewPlugin(
        file = File(options.pluginPath!!),
        tags = options.tags.toList(),
        licenseUrl = LicenseUrl.fromString(options.licenseUrl),
        family = family,
        vendor = options.vendor,
        channel = parseChannel(options.channel),
        isHidden = options.isHidden
      )
    }

    private fun uploadPluginUpdate(uploader: PluginUploader, options: UploadOptions): PluginUpdateBean {
      val pluginId = options.pluginId!!
      val intPluginId = pluginId.toIntOrNull()
      val family = options.family

      val result = when {
        intPluginId != null -> {
          println("Plugin #$intPluginId update upload has started.")
          uploader.upload(
            id = intPluginId,
            file = File(options.pluginPath!!),
            channel = parseChannel(options.channel),
            notes = options.notes,
            isHidden = options.isHidden
          )
        }
        family != null -> {
          println("$family plugin \"$pluginId\" update upload has started.")

          uploader.uploadUpdateByXmlIdAndFamily(
            id = pluginId,
            family = family,
            file = File(options.pluginPath!!),
            channel = parseChannel(options.channel),
            notes = options.notes,
            isHidden = options.isHidden
          )
        }
        else -> {
          error("Uploading updates by string plugin-id without specifying the `family` is not supported. Please update your configuration.")
        }
      }

      println("Plugin update #${result.id} has been successfully uploaded.")
      return result
    }

    private fun info(args: Array<String>) {
      val options = InfoOptions()
      Args.parseOrExit(options, args)
      val pluginRepository = PluginRepositoryFactory.create(
        options.host).pluginManager
      val plugin = pluginRepository.getPluginByXmlId(options.pluginId!!, options.family!!)
      if (plugin != null) {
        println("${plugin.name} ${plugin.id} made by ${plugin.vendor?.name}")
      } else {
        println("Plugin is not found!")
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

    @set:Argument("license", description = "Url to plugin license")
    var licenseUrl: String = ""

    @set:Argument("tags", description = "Tags for the plugin")
    var tags: Array<String> = emptyArray()

    @set:Argument("family", description = "Plugin's family")
    var family: ProductFamily? = null

    @set:Argument(description = "Change notes (may include HTML tags). The argument is ignored when uploading updates for IntelliJ-based IDEs")
    var notes: String? = null

    @set:Argument(description = "Id of vendor under which the new plugin should be uploaded")
    var vendor: String? = null

    @set:Argument("isHidden", required = false, description = "Should the new uploading plugin or update be hidden")
    var isHidden: Boolean = false
  }

  class DownloadOptions : BaseOptions() {
    @set:Argument("plugin", required = true, description = "Plugin ID defined in plugin.xml")
    var pluginId: String? = null

    @set:Argument("version", description = "Plugin version to download")
    var version: String? = null

    @set:Argument("ide-build", description = "IDE build number with product code to download plugin compatible with (e.g. IC-145.184)")
    var ideBuild: String? = null

    @set:Argument("prev", description = "Previous plugin's version archive file path")
    var oldFile: String = ""

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
    var family: ProductFamily? = ProductFamily.INTELLIJ
  }

  open class BaseOptions {
    @set:Argument(description = "Plugins repository host")
    var host = "https://plugins.jetbrains.com"

    @set:Argument(description = "Plugin channel")
    var channel: String? = null
  }
}
