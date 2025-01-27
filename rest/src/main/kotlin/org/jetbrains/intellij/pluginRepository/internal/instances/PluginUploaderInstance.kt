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
  override fun uploadNewPlugin(
    file: File,
    tags: List<String>,
    licenseUrl: LicenseUrl,
    family: ProductFamily,
    vendor: String?,
    channel: String?,
    isHidden: Boolean
  ): PluginBean {
    return baseUploadPlugin(file) {
      require(vendor == null || vendor.isNotBlank()) { Messages.getMessage("empty.vendor") }
      require(tags.isNotEmpty()) { Messages.getMessage("empty.tags") }
      require(licenseUrl.url.isNotEmpty()) { Messages.getMessage("empty.license.url") }
      val license = URL(licenseUrl.url).toExternalForm().toRequestBody()
      val requestTags = tags.map { it.toRequestBody() }
      uploadOrFail(service.uploadNewPlugin(
        file = file.toMultipartBody(),
        family = family.id,
        licenseUrl = license,
        tags = ArrayList(requestTags),
        vendor = vendor?.toRequestBody(),
        channel = channel?.toRequestBody(),
        isHidden = isHidden
      ))
    }
  }

  private fun baseUploadPlugin(file: File, block: () -> PluginBean): PluginBean {
    LOG.info("Uploading new plugin from ${file.absolutePath}")
    val plugin = block()
    LOG.info("${plugin.name} was successfully uploaded with id ${plugin.id}")
    return plugin
  }

  override fun upload(id: PluginId, file: File, channel: String?, notes: String?, isHidden: Boolean): PluginUpdateBean {
    return uploadOrFail(
      service.uploadById(
        id,
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        isHidden,
        file.toMultipartBody(),
      )
    )
  }

  @Deprecated("Use uploadUpdateByXmlIdAndFamily(id, file, channel, notes, isHidden, family)")
  override fun upload(
    id: StringPluginId,
    file: File,
    channel: String?,
    notes: String?,
    isHidden: Boolean
  ): PluginUpdateBean {
    return uploadOrFail(
      service.uploadByStringId(
        id.toRequestBody(),
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        isHidden,
        file.toMultipartBody()
      )
    )
  }

  override fun uploadUpdateByXmlIdAndFamily(
    id: StringPluginId,
    family: ProductFamily,
    file: File,
    channel: String?,
    notes: String?,
    isHidden: Boolean
  ): PluginUpdateBean {
    return uploadOrFail(
      service.uploadByStringIdAndFamily(
        id.toRequestBody(),
        family = family.id.toRequestBody(),
        channel?.toRequestBody(),
        notes?.toRequestBody(),
        isHidden,
        file.toMultipartBody()
      )
    )
  }
}
