package org.jetbrains.intellij.pluginRepository

import org.jetbrains.intellij.pluginRepository.model.CompatibleUpdateBean
import org.jetbrains.intellij.pluginRepository.model.PluginBean
import org.jetbrains.intellij.pluginRepository.model.PluginUpdateBean
import org.jetbrains.intellij.pluginRepository.model.PluginUserBean
import org.jetbrains.intellij.pluginRepository.model.IntellijUpdateMetadata
import org.jetbrains.intellij.pluginRepository.model.ProductEnum
import org.jetbrains.intellij.pluginRepository.model.ProductFamily
import org.jetbrains.intellij.pluginRepository.model.PluginXmlBean
import java.io.File

interface PluginRepository {
  val pluginManager: PluginManager
  val pluginUpdateManager: PluginUpdateManager
  val downloader: PluginDownloader
  val uploader: PluginUploader
}

interface PluginManager {
  /**
   * Plugin info by [xmlId] & [ProductFamily].
   * @param xmlId id from plugin descriptor file. Example: "org.jetbrains.kotlin"
   */
  fun getPluginByXmlId(xmlId: String, family: ProductFamily = ProductFamily.INTELLIJ): PluginBean?

  /**
   * Plugin info by [id].
   * Supported for all [ProductFamily].
   */
  fun getPlugin(id: Int): PluginBean?

  /**
   * List of plugin authors.
   */
  fun getPluginDevelopers(id: Int): List<PluginUserBean>

  /**
   * List of plugins channels.
   * Example: "", "EAP" and etc.
   */
  fun getPluginChannels(id: Int): List<String>

  /**
   * The list of compatible products for a plugin.
   */
  fun getPluginCompatibleProducts(id: Int): List<ProductEnum>

  /**
   * Getting plugin XML ids by [dependency]. Examples [dependency]: "com.intellij.modules.java", "(optional) org.jetbrains.java.decompiler".
   * @return: list of plugin xml ids. Example: "org.jetbrains.kotlin
   */
  fun getPluginXmlIdByDependency(dependency: String, includeOptional: Boolean = true): List<String>

  /**
   * List of plugins compatible with [ideBuild]
   */
  fun listPlugins(ideBuild: String, channel: String? = null, pluginId: String? = null): List<PluginXmlBean>

  /**
   * List of plugins xml ids compatible with [build].
   * Supported for [ProductFamily.INTELLIJ].
   * @param max max result set. Max: 10000 - [offset]
   * @param query query for search.
   */
  fun getCompatiblePluginsXmlIds(build: String, max: Int, offset: Int, query: String = ""): List<String>

  /**
   * Search last compatible update for each id from [xmlIds]
   * Supported for [ProductFamily.INTELLIJ].
   * @param channel plugin channel. Default value is "stable" plugin channel.
   * @return the list of last compatible updates [CompatibleUpdateBean] for plugins from [xmlIds].
   */
  fun searchCompatibleUpdates(xmlIds: List<String>, build: String, channel: String = ""): List<CompatibleUpdateBean>
}

interface PluginUpdateManager {
  /**
   * Get list of plugin updates by plugin [xmlId] and [version] and [family].
   * @param version - version of the plugin.
   * @return the list of updates [PluginUpdateBean]. There could be a several updates for some OLD plugins/updates.
   */
  fun getUpdatesByVersionAndFamily(xmlId: String, version: String, family: ProductFamily = ProductFamily.INTELLIJ): List<PluginUpdateBean>

  /**
   * Get plugin update by [id]. If needed to get a lot of plugin updates recommended using [getIntellijUpdateMetadata].
   * Supported for all [ProductFamily].
   */
  fun getUpdateById(id: Int): PluginUpdateBean?

  /**
   * Getting plugin update metadata.
   * Use for getting a big list of plugin updates.
   * Supported for [ProductFamily.INTELLIJ].
   */
  fun getIntellijUpdateMetadata(pluginId: Int, updateId: Int): IntellijUpdateMetadata?
}

interface PluginDownloader {
  /**
   * Download [ProductFamily.INTELLIJ] plugin by plugin XML id.
   * @param xmlId plugin XML id. Example: "org.jetbrains.kotlin" for Kotlin plugin.
   * @param version version of the plugin. Example: "1.3.61-release-IJ2019.3-1" for Kotlin plugin.
   * @param channel plugin channel. Default value is "stable" plugin channel.
   */
  fun download(xmlId: String, version: String, targetPath: File, channel: String? = null): File?

  /**
   * Download latest compatible update for plugin [ProductFamily.INTELLIJ] by IDE Version.
   * @param xmlId plugin XML id.
   * @param ideBuild IDE version. Example: "IC-145.184"
   * @param channel plugin channel. Default value is "stable" plugin channel.
   */
  fun downloadLatestCompatiblePlugin(xmlId: String, ideBuild: String, targetPath: File, channel: String? = null): File?
}

interface PluginUploader {
  /**
   * Upload plugin by ID into specific channel.
   * *Important*: plugin notes will be ignored for ides based on intellij platform ([ProductFamily.INTELLIJ]).
   * Plugin notes for teamcity plugins and hub widgets only. For IDE plugins use <changed-notes> element in plugin.xml
   * @param channel plugin channel. Default value is "stable" plugin channel.
   * @param notes plugin update notes.
   */
  fun uploadPlugin(id: Int, file: File, channel: String? = null, notes: String? = null)

  /**
   * Upload plugin by Xml id into specific channel.
   * *Important*: plugin notes will be ignored for ides based on intellij platform ([ProductFamily.INTELLIJ]).
   * Plugin notes for teamcity plugins and hub widgets only. For IDE plugins use <changed-notes> element in plugin.xml
   * @param channel plugin channel. Default value is "stable" plugin channel.
   * @param notes plugin update notes.
   */
  fun uploadPlugin(xmlId: String, file: File, channel: String? = null, notes: String? = null)

  /**
   * Upload a new plugin into JetBrains Marketplace.
   * Make sure you accepted all agreements on the Marketplace web-site: https://plugins.jetbrains.com/.
   * @param categoryId tag id. Example: https://plugins.jetbrains.com/idea.
   * @param licenseUrl link to the license.
   */
  fun uploadNewPlugin(
    file: File,
    categoryId: Int,
    licenseUrl: String,
    family: ProductFamily = ProductFamily.INTELLIJ
  ): PluginBean
}