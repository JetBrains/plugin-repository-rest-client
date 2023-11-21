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

  @Deprecated("Use uploadNewPlugin(file, tags, licenseUrl, family, vendor)")
  override fun uploadNewPlugin(
    file: File,
    categoryId: Int,
    licenseUrl: String,
    family: ProductFamily,
    vendor: String?
  ): PluginBean {
    return baseUploadPlugin(file) {
      require(vendor == null || vendor.isNotBlank()) { Messages.getMessage("empty.vendor") }
      uploadOrFail(service.uploadNewPlugin(
        file = file.toMultipartBody(),
        family = family.id,
        licenseUrl = licenseUrl.toRequestBody(),
        category = categoryId,
        vendor = vendor?.toRequestBody()
      ))
    }
  }

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


  @Deprecated("Use upload(id, file, channel, notes)")
  override fun uploadPlugin(id: PluginId, file: File, channel: String?, notes: String?) {
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

  @Deprecated("Use upload(id, file, channel, notes)")
  override fun uploadPlugin(xmlId: StringPluginId, file: File, channel: String?, notes: String?) {
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
        file.toMultipartBody(),
      )
    )
  }
}
