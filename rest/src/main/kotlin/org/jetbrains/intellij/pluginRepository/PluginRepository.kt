package org.jetbrains.intellij.pluginRepository

import org.jetbrains.intellij.pluginRepository.model.json.PluginInfoBean
import org.jetbrains.intellij.pluginRepository.model.xml.PluginBean
import java.io.File

interface PluginRepository {
  fun listPlugins(ideBuild: String, channel: String? = null, pluginId: String? = null): List<PluginBean>

  fun fetchPluginInfo(family: String, pluginXmlId: String): PluginInfoBean?

  fun download(pluginXmlId: String, version: String, channel: String? = null, targetPath: File): File?

  fun downloadCompatiblePlugin(pluginXmlId: String, ideBuild: String, channel: String? = null, targetPath: File): File?

  fun uploadPlugin(pluginId: Int, file: File, channel: String? = null, notes: String? = null)

  fun uploadPlugin(pluginXmlId: String, file: File, channel: String? = null, notes: String? = null)

  fun uploadNewPlugin(file: File, family: String, categoryId: Int, licenseUrl: String): PluginInfoBean
}