package org.jetbrains.intellij.pluginRepository

import org.jetbrains.intellij.pluginRepository.model.json.CompatibleUpdateBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginUpdateBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginUserBean
import org.jetbrains.intellij.pluginRepository.model.repository.IntellijUpdateMetadata
import org.jetbrains.intellij.pluginRepository.model.repository.ProductEnum
import org.jetbrains.intellij.pluginRepository.model.repository.ProductFamily
import org.jetbrains.intellij.pluginRepository.model.xml.PluginXmlBean
import java.io.File

interface PluginRepository {
  val pluginManager: PluginManager
  val pluginUpdateManager: PluginUpdateManager
  val downloader: PluginDownloader
  val uploader: PluginUploader
}

interface PluginManager {
  fun getPluginByXmlId(xmlId: String, family: ProductFamily = ProductFamily.INTELLIJ): PluginBean?
  fun getPlugin(id: Int): PluginBean?
  fun getPluginDevelopers(id: Int): List<PluginUserBean>
  fun getPluginChannels(id: Int): List<String>
  fun getPluginCompatibleProducts(id: Int): List<ProductEnum>
  fun getPluginXmlIdByDependency(dependency: String, includeOptional: Boolean = true): List<String>
  fun listPlugins(ideBuild: String, channel: String? = null, pluginId: String? = null): List<PluginXmlBean>
  fun getCompatiblePluginsXmlIds(build: String, max: Int, offset: Int): List<String>
  fun getCompatibleUpdate(xmlId: String, build: String, max: Int, channel: String = ""): List<CompatibleUpdateBean>
}

interface PluginUpdateManager {
  fun getUpdatesByVersionAndFamily(xmlId: String, version: String, family: ProductFamily = ProductFamily.INTELLIJ): List<PluginUpdateBean>
  fun getUpdateById(id: Int): PluginUpdateBean?
  fun getIntellijUpdateMetadata(pluginId: Int, updateId: Int): IntellijUpdateMetadata?
}

interface PluginDownloader {
  /**
   * Download [ProductFamily.INTELLIJ] plugin by plugin XML id.
   * @param xmlId   - plugin XML id. Example: "org.jetbrains.kotlin" for Kotlin plugin.
   * @param version - version of the plugin. Example: "1.3.61-release-IJ2019.3-1" for Kotlin plugin.
   * @param channel - plugin channel. Default value is "stable" plugin channel.
   */
  fun download(xmlId: String, version: String, targetPath: File, channel: String? = null): File?

  /**
   * Download latest compatible update for plugin [ProductFamily.INTELLIJ] by IDE Version.
   * @param xmlId    - plugin XML id.
   * @param ideBuild - IDE version. Example: "IC-145.184"
   * @param channel  - plugin channel. Default value is "stable" plugin channel.
   */
  fun downloadLatestCompatiblePlugin(xmlId: String, ideBuild: String, targetPath: File, channel: String? = null): File?
}

interface PluginUploader {
  /**
   * Upload plugin by ID into specific channel.
   * IMPORTANT: PLUGIN NOTES WILL BE IGNORED FOR IDES BASED ON INTELLIJ PLATFORM ([ProductFamily.INTELLIJ]).
   * @param channel   - plugin channel. Default value is "stable" plugin channel.
   * @param notes     - plugin update notes.
   */
  fun uploadPlugin(id: Int, file: File, channel: String? = null, notes: String? = null)

  /**
   * Upload plugin by Xml id into specific channel.
   * IMPORTANT: PLUGIN NOTES WILL BE IGNORED FOR IDES BASED ON INTELLIJ PLATFORM ([ProductFamily.INTELLIJ]).
   * @param channel   - plugin channel. Default value is "stable" plugin channel.
   * @param notes     - plugin update notes.
   */
  fun uploadPlugin(xmlId: String, file: File, channel: String? = null, notes: String? = null)

  /**
   * Upload a new plugin into JetBrains Marketplace.
   * Make sure you accepted all agreements on the Marketplace web-site: https://plugins.jetbrains.com/.
   * @param categoryId - tag id. Example: https://plugins.jetbrains.com/idea.
   * @param licenseUrl - link to the license.
   */
  fun uploadNewPlugin(
    file: File,
    categoryId: Int,
    licenseUrl: String,
    family: ProductFamily = ProductFamily.INTELLIJ
  ): PluginBean
}