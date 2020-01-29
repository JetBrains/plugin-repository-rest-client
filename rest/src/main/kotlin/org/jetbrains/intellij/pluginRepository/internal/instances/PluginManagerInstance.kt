package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.PluginManager
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.CompatibleUpdateRequest
import org.jetbrains.intellij.pluginRepository.internal.utils.executeAndParseBody
import org.jetbrains.intellij.pluginRepository.model.UpdateBean
import org.jetbrains.intellij.pluginRepository.model.PluginBean
import org.jetbrains.intellij.pluginRepository.model.PluginUserBean
import org.jetbrains.intellij.pluginRepository.model.ProductEnum
import org.jetbrains.intellij.pluginRepository.model.ProductFamily
import org.jetbrains.intellij.pluginRepository.model.PluginXmlBean
import org.jetbrains.intellij.pluginRepository.internal.utils.convertCategory

internal class PluginManagerInstance(private val service: PluginRepositoryService) : PluginManager {

  override fun listPlugins(ideBuild: String, channel: String?, pluginId: String?): List<PluginXmlBean> {
    val response = executeAndParseBody(service.listPlugins(ideBuild, channel, pluginId))
    return response?.categories?.flatMap { convertCategory(it) } ?: emptyList()
  }

  override fun getCompatiblePluginsXmlIds(build: String, max: Int, offset: Int, query: String): List<String> =
    executeAndParseBody(service.searchPluginsXmlIds(build, max, offset, query)) ?: emptyList()

  override fun searchCompatibleUpdates(xmlIds: List<String>, build: String, channel: String): List<UpdateBean> =
    executeAndParseBody(service.searchLastCompatibleUpdate(CompatibleUpdateRequest(xmlIds, build, channel))) ?: emptyList()

  override fun getPluginByXmlId(xmlId: String, family: ProductFamily): PluginBean? = executeAndParseBody(
    service.getPluginByXmlId(family.id, xmlId))

  override fun getPlugin(id: Int): PluginBean? = executeAndParseBody(service.getPluginById(id))

  override fun getPluginVersions(id: Int): List<UpdateBean> {
    val pluginBean = executeAndParseBody(service.getPluginById(id)) ?: return emptyList()
    return executeAndParseBody(service.getPluginVersions(id)).orEmpty().map {
      UpdateBean(it.id, pluginBean.id, pluginBean.xmlId, it.version)
    }
  }

  override fun getPluginDevelopers(id: Int): List<PluginUserBean> = executeAndParseBody(service.getPluginDevelopers(id)) ?: emptyList()

  override fun getPluginChannels(id: Int): List<String> = executeAndParseBody(service.getPluginChannels(id)) ?: emptyList()

  override fun getPluginCompatibleProducts(id: Int): List<ProductEnum> =
    executeAndParseBody(service.getPluginCompatibleProducts(id)) ?: emptyList()

  override fun getPluginXmlIdByDependency(dependency: String, includeOptional: Boolean) =
    executeAndParseBody(service.getPluginXmlIdByDependency(dependency, includeOptional)) ?: emptyList()

}
