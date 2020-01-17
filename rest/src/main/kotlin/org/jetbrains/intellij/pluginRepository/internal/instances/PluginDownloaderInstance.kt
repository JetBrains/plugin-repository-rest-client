package org.jetbrains.intellij.pluginRepository.internal.instances

import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.LOG
import org.jetbrains.intellij.pluginRepository.PluginDownloader
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.downloadPlugin
import retrofit2.Call
import java.io.File


internal class PluginDownloaderInstance(private val service: PluginRepositoryService) : PluginDownloader {

  override fun download(xmlId: String, version: String, channel: String?, targetPath: File): File? {
    LOG.info("Downloading $xmlId:$version")
    return doDownloadPlugin(service.download(xmlId, version, channel), targetPath)
  }

  override fun downloadLatestCompatiblePlugin(
    xmlId: String,
    ideBuild: String,
    channel: String?,
    targetPath: File
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