package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.PluginUpdateManager
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.executeAndParseBody
import org.jetbrains.intellij.pluginRepository.internal.utils.executeExceptionallyBatch
import org.jetbrains.intellij.pluginRepository.model.*

internal class PluginUpdateManagerInstance(private val service: PluginRepositoryService) : PluginUpdateManager {

  override fun getUpdatesByVersionAndFamily(xmlId: StringPluginId, version: String, family: ProductFamily): List<PluginUpdateBean> =
    executeAndParseBody(service.getUpdatesByVersionAndFamily(xmlId, version, family.id), nullFor404 = true) ?: emptyList()

  override fun getUpdateById(id: UpdateId): PluginUpdateBean? =
    executeAndParseBody(service.getUpdateById(id), nullFor404 = true)

  override fun getIntellijUpdateMetadata(pluginId: PluginId, updateId: UpdateId): IntellijUpdateMetadata? =
    executeAndParseBody(service.getIntelliJUpdateMeta(pluginId, updateId), nullFor404 = true)

  override fun getIntellijUpdateMetadataBatch(updateIds: List<Pair<PluginId, UpdateId>>): Map<UpdateId, IntellijUpdateMetadata> {
    val calls = updateIds.map { (pluginId, updateId) -> service.getIntelliJUpdateMeta(pluginId, updateId) }
    val responses = executeExceptionallyBatch(calls).values
    val result = hashMapOf<UpdateId, IntellijUpdateMetadata>()
    for (response in responses) {
      if (response.isSuccessful) {
        val metadata = response.body()
        if (metadata != null) {
          result[metadata.id] = metadata
        }
      }
    }
    return result
  }

}
