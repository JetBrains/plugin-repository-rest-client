package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.PluginManager
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.executeAndParseBody
import org.jetbrains.intellij.pluginRepository.model.json.CompatibleUpdateBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginUserBean
import org.jetbrains.intellij.pluginRepository.model.repository.ProductEnum
import org.jetbrains.intellij.pluginRepository.model.repository.ProductFamily
import org.jetbrains.intellij.pluginRepository.model.xml.PluginXmlBean
import org.jetbrains.intellij.pluginRepository.model.xml.converters.convertCategory

internal class PluginManagerInstance(private val service: PluginRepositoryService) : PluginManager {

  override fun listPlugins(ideBuild: String, channel: String?, pluginId: String?): List<PluginXmlBean> {
    val response = executeAndParseBody(service.listPlugins(ideBuild, channel, pluginId))
    return response?.categories?.flatMap { convertCategory(it) } ?: emptyList()
  }

  override fun getCompatiblePluginsXmlIds(build: String, max: Int, offset: Int): List<String> =
    executeAndParseBody(service.searchPluginsXmlIds(build, max, offset)) ?: emptyList()

  override fun getCompatibleUpdate(xmlId: String, build: String, max: Int, channel: String): List<CompatibleUpdateBean> =
    executeAndParseBody(service.getLastCompatibleUpdate(xmlId, build, channel, max)) ?: emptyList()

  override fun getPluginByXmlId(xmlId: String, family: ProductFamily): PluginBean? = executeAndParseBody(
    service.getPluginByXmlId(family.id, xmlId))

  override fun getPlugin(id: Int): PluginBean? = executeAndParseBody(service.getPluginById(id))

  override fun getPluginDevelopers(id: Int): List<PluginUserBean> = executeAndParseBody(service.getPluginDevelopers(id)) ?: emptyList()

  override fun getPluginChannels(id: Int): List<String> = executeAndParseBody(service.getPluginChannels(id)) ?: emptyList()

  override fun getPluginCompatibleProducts(id: Int): List<ProductEnum> =
    executeAndParseBody(service.getPluginCompatibleProducts(id)) ?: emptyList()

  override fun getPluginXmlIdByDependency(dependency: String, includeOptional: Boolean) =
    executeAndParseBody(service.getPluginXmlIdByDependency(dependency, includeOptional)) ?: emptyList()

}
