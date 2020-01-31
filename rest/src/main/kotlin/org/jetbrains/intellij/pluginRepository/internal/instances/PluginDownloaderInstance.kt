package org.jetbrains.intellij.pluginRepository.internal.instances

import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.PluginDownloader
import org.jetbrains.intellij.pluginRepository.internal.api.LOG
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.downloadPlugin
import org.jetbrains.intellij.pluginRepository.model.UpdateId
import retrofit2.Call
import java.io.File


internal class PluginDownloaderInstance(private val service: PluginRepositoryService) : PluginDownloader {

  override fun download(xmlId: String, version: String, targetPath: File, channel: String?): File? {
    LOG.info("Downloading $xmlId:$version")
    return doDownloadPlugin(service.download(xmlId, version, channel), targetPath)
  }

  override fun download(id: UpdateId, targetPath: File): File? {
    LOG.info("Downloading update of plugin for $id...")
    return doDownloadPlugin(service.download(id), targetPath)
  }

  override fun downloadLatestCompatiblePlugin(
    xmlId: String,
    ideBuild: String,
    targetPath: File,
    channel: String?
  ): File? {
    LOG.info("Downloading $xmlId for $ideBuild build")
    return doDownloadPlugin(service.downloadCompatiblePlugin(xmlId, ideBuild, channel), targetPath)
  }

  private fun doDownloadPlugin(callable: Call<ResponseBody>, targetPath: File): File? {
    val file = downloadPlugin(callable, targetPath)
    LOG.info("Downloaded successfully to $targetPath")
    return file
  }

}