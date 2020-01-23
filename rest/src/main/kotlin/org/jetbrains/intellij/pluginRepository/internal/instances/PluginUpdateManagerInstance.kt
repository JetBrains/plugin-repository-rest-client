package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.PluginUpdateManager
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.executeAndParseBody
import org.jetbrains.intellij.pluginRepository.model.PluginUpdateBean
import org.jetbrains.intellij.pluginRepository.model.IntellijUpdateMetadata
import org.jetbrains.intellij.pluginRepository.model.ProductFamily

internal class PluginUpdateManagerInstance(private val service: PluginRepositoryService) : PluginUpdateManager {

  override fun getUpdatesByVersionAndFamily(xmlId: String, version: String, family: ProductFamily): List<PluginUpdateBean> =
    executeAndParseBody(service.getUpdatesByVersionAndFamily(xmlId, version, family.id)) ?: emptyList()

  override fun getUpdateById(id: Int): PluginUpdateBean? = executeAndParseBody(service.getUpdateById(id))

  override fun getIntellijUpdateMetadata(pluginId: Int, updateId: Int): IntellijUpdateMetadata? =
    executeAndParseBody(service.getIntelliJUpdateMeta(pluginId, updateId))

}