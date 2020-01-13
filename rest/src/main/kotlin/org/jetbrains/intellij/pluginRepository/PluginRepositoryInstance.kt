package org.jetbrains.intellij.pluginRepository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.intellij.pluginRepository.internal.Messages
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.downloadPlugin
import org.jetbrains.intellij.pluginRepository.internal.utils.executeAndParseBody
import org.jetbrains.intellij.pluginRepository.internal.utils.uploadOrFail
import org.jetbrains.intellij.pluginRepository.model.json.PluginInfoBean
import org.jetbrains.intellij.pluginRepository.model.xml.PluginBean
import org.jetbrains.intellij.pluginRepository.model.xml.converters.convertCategory
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * @param siteUrl url of plugins repository instance. For example: https://plugins.jetbrains.com
 * @param token hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
 */
class PluginRepositoryInstance(private val siteUrl: String, private val token: String? = null) : PluginRepository {

  private val service = Retrofit.Builder()
    .baseUrl(siteUrl)
    .client(
      OkHttpClient()
        .newBuilder()
        .dispatcher(Dispatcher(Executors.newCachedThreadPool(DaemonThreadFactory("retrofit-thread"))))
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(object : Interceptor {
          override fun intercept(chain: Interceptor.Chain): Response {
            val request = if (token != null) {
              chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
              chain.request()
            }
            return chain.proceed(request)
          }
        })
        .build())
    .addConverterFactory(JaxbConverterFactory.create())
    .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
    .build()
    .create(PluginRepositoryService::class.java)

  override fun listPlugins(ideBuild: String, channel: String?, pluginId: String?): List<PluginBean> {
    val response = executeAndParseBody(service.listPlugins(ideBuild, channel, pluginId))
    return response?.categories?.flatMap { convertCategory(it) } ?: emptyList()
  }

  override fun fetchPluginInfo(family: String, pluginXmlId: String): PluginInfoBean? {
    return executeAndParseBody(service.pluginInfo(family, pluginXmlId))
  }

  override fun download(pluginXmlId: String, version: String, channel: String?, targetPath: File): File? {
    LOG.info("Downloading $pluginXmlId:$version")
    return doDownloadPlugin(service.download(pluginXmlId, version, channel), targetPath)
  }

  override fun downloadCompatiblePlugin(
    pluginXmlId: String,
    ideBuild: String,
    channel: String?,
    targetPath: File
  ): File? {
    LOG.info("Downloading $pluginXmlId for $ideBuild build")
    return doDownloadPlugin(service.downloadCompatiblePlugin(pluginXmlId, ideBuild, channel), targetPath)
  }

  override fun uploadNewPlugin(file: File, family: String, categoryId: Int, licenseUrl: String): PluginInfoBean {
    ensureCredentialsAreSet()
    LOG.info("Uploading new plugin from ${file.absolutePath}")
    val plugin = uploadOrFail(service.uploadNewPlugin(file.toMultipartBody(), family, licenseUrl.toRequestBody(), categoryId))
    LOG.info("${plugin.name} was successfully uploaded with id ${plugin.id}")
    return plugin
  }

  override fun uploadPlugin(pluginId: Int, file: File, channel: String?, notes: String?) {
    uploadPluginInternal(file, pluginId = pluginId, channel = channel, notes = notes)
  }

  override fun uploadPlugin(pluginXmlId: String, file: File, channel: String?, notes: String?) {
    uploadPluginInternal(file, pluginXmlId = pluginXmlId, channel = channel, notes = notes)
  }

  private fun doDownloadPlugin(callable: Call<ResponseBody>, targetPath: File): File? {
    val file = downloadPlugin(callable, targetPath)
    LOG.info("Downloaded successfully to $targetPath")
    return file
  }

  private fun uploadPluginInternal(
    file: File,
    pluginId: Int? = null,
    pluginXmlId: String? = null,
    channel: String? = null,
    notes: String? = null
  ) {
    if (pluginXmlId == null && pluginId == null) {
      throw IllegalArgumentException(Messages.getMessage("missing.plugins.parameters"))
    }
    ensureCredentialsAreSet()
    val channelAsRequestBody = channel?.toRequestBody()
    val notesAsRequestBody = notes?.toRequestBody()
    val multipartFile = file.toMultipartBody()
    val message = if (pluginXmlId != null) {
      uploadOrFail(
        service.uploadByXmlId(pluginXmlId.toRequestBody(), channelAsRequestBody, notesAsRequestBody, multipartFile),
        pluginXmlId
      )
    } else {
      uploadOrFail(
        service.upload(pluginId!!, channelAsRequestBody, notesAsRequestBody, multipartFile),
        pluginId.toString()
      )
    }
    LOG.info("Uploading of plugin is done: ${message.string()}")
  }

  private fun ensureCredentialsAreSet() {
    if (token == null) throw RuntimeException(Messages.getMessage("missing.token"))
  }

  private fun File.toMultipartBody(): MultipartBody.Part {
    val body = this.asRequestBody("application/octet-stream".toMediaType())
    return MultipartBody.Part.createFormData("file", this.name, body)
  }

  private fun String.toRequestBody() = this.toRequestBody("text/plain".toMediaType())

  private companion object {
    private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")
  }

  private class DaemonThreadFactory(private val threadNamePrefix: String) : ThreadFactory {

    private val defaultThreadFactory = Executors.defaultThreadFactory()

    private val nameCount = AtomicLong()

    override fun newThread(r: Runnable): Thread {
      val thread = defaultThreadFactory.newThread(r)
      thread.name = "$threadNamePrefix-${nameCount.getAndIncrement()}"
      thread.isDaemon = true
      return thread
    }
  }
}