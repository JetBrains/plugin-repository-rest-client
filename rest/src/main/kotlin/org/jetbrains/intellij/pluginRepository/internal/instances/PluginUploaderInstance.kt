package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.PluginUploader
import org.jetbrains.intellij.pluginRepository.internal.Messages
import org.jetbrains.intellij.pluginRepository.internal.api.LOG
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.toMultipartBody
import org.jetbrains.intellij.pluginRepository.internal.utils.toRequestBody
import org.jetbrains.intellij.pluginRepository.internal.utils.uploadOrFail
import org.jetbrains.intellij.pluginRepository.model.*
import java.io.File
import java.net.URL

internal class PluginUploaderInstance(private val service: PluginRepositoryService) : PluginUploader {
  companion object {
    const val MAX_FILE_SIZE = 419430400L // 400MB
  }

  override fun uploadNewPlugin(file: File, categoryId: Int, licenseUrl: String, family: ProductFamily): PluginBean {
    return baseUploadPlugin(file) {
      uploadOrFail(service.uploadNewPlugin(file.toMultipartBody(), family.id, licenseUrl.toRequestBody(), categoryId))
    }
  }

  override fun uploadNewPlugin(
    file: File,
    tags: List<String>,
    licenseUrl: LicenseUrl,
    family: ProductFamily
  ): PluginBean {
    return baseUploadPlugin(file) {
      if (tags.isEmpty()) {
        throw IllegalArgumentException("Tags should not be empty")
      }
      val license = URL(licenseUrl.url).toExternalForm().toRequestBody()
      val requestTags = tags.map { it.toRequestBody() }
      uploadOrFail(service.uploadNewPlugin(file.toMultipartBody(), family.id, license, ArrayList(requestTags)))
    }
  }

  private fun baseUploadPlugin(file: File, block: () -> PluginBean): PluginBean {
    if (file.length() > MAX_FILE_SIZE) {
      throw IllegalArgumentException(Messages.getMessage("max.file.size"))
    }
    LOG.info("Uploading new plugin from ${file.absolutePath}")
    val plugin = block()
    LOG.info("${plugin.name} was successfully uploaded with id ${plugin.id}")
    return plugin
  }


  override fun uploadPlugin(id: PluginId, file: File, channel: String?, notes: String?) {
    validateSize(file)
    uploadOrFail(
      service.upload(
        id,
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        file.toMultipartBody(),
      )
    )
    LOG.info("Uploading of plugin is done")
  }

  override fun uploadPlugin(xmlId: StringPluginId, file: File, channel: String?, notes: String?) {
    validateSize(file)
    uploadOrFail(
      service.uploadByXmlId(
        xmlId.toRequestBody(),
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        file.toMultipartBody(),
      )
    )
    LOG.info("Uploading of plugin is done")
  }

  override fun upload(id: PluginId, file: File, channel: String?, notes: String?): PluginUpdateBean {
    validateSize(file)
    return uploadOrFail(
      service.uploadById(
        id,
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        file.toMultipartBody(),
      )
    )
  }

  override fun upload(id: StringPluginId, file: File, channel: String?, notes: String?): PluginUpdateBean {
    validateSize(file)
    return uploadOrFail(
      service.uploadByStringId(
        id.toRequestBody(),
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        file.toMultipartBody(),
      )
    )
  }

  private fun validateSize(file: File) {
    if (file.length() > MAX_FILE_SIZE) {
      throw IllegalArgumentException(Messages.getMessage("max.file.size"))
    }
  }

}
