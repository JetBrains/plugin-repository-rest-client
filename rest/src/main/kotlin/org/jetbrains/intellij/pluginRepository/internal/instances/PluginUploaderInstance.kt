package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.PluginUploader
import org.jetbrains.intellij.pluginRepository.internal.Messages
import org.jetbrains.intellij.pluginRepository.internal.api.LOG
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.toMultipartBody
import org.jetbrains.intellij.pluginRepository.internal.utils.toRequestBody
import org.jetbrains.intellij.pluginRepository.internal.utils.uploadOrFail
import org.jetbrains.intellij.pluginRepository.model.PluginBean
import org.jetbrains.intellij.pluginRepository.model.PluginId
import org.jetbrains.intellij.pluginRepository.model.PluginXmlId
import org.jetbrains.intellij.pluginRepository.model.ProductFamily
import java.io.File

internal class PluginUploaderInstance(private val service: PluginRepositoryService) : PluginUploader {

  override fun uploadNewPlugin(file: File, categoryId: Int, licenseUrl: String, family: ProductFamily): PluginBean {
    LOG.info("Uploading new plugin from ${file.absolutePath}")
    val plugin = uploadOrFail(service.uploadNewPlugin(file.toMultipartBody(), family.id, licenseUrl.toRequestBody(), categoryId))
    LOG.info("${plugin.name} was successfully uploaded with id ${plugin.id}")
    return plugin
  }

  override fun uploadPlugin(id: PluginId, file: File, channel: String?, notes: String?) {
    uploadPluginInternal(file, pluginId = id, channel = channel, notes = notes)
  }

  override fun uploadPlugin(xmlId: PluginXmlId, file: File, channel: String?, notes: String?) {
    uploadPluginInternal(file, pluginXmlId = xmlId, channel = channel, notes = notes)
  }

  private fun uploadPluginInternal(
    file: File,
    pluginId: PluginId? = null,
    pluginXmlId: PluginXmlId? = null,
    channel: String? = null,
    notes: String? = null
  ) {
    if (pluginXmlId == null && pluginId == null) {
      throw IllegalArgumentException(Messages.getMessage("missing.plugins.parameters"))
    }
    val channelAsRequestBody = channel?.toRequestBody()
    val notesAsRequestBody = notes?.toRequestBody()
    val multipartFile = file.toMultipartBody()
    val message = if (pluginXmlId != null) {
      uploadOrFail(
        service.uploadByXmlId(pluginXmlId.toRequestBody(), channelAsRequestBody, notesAsRequestBody, multipartFile),
        pluginXmlId
      )
    }
    else {
      uploadOrFail(
        service.upload(pluginId!!, channelAsRequestBody, notesAsRequestBody, multipartFile),
        pluginId.toString()
      )
    }
    LOG.info("Uploading of plugin is done: ${message.string()}")
  }

}