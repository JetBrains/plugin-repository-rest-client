package org.jetbrains.intellij.pluginRepository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.exceptions.PluginUploadRestError
import org.jetbrains.intellij.pluginRepository.exceptions.restException
import org.jetbrains.intellij.pluginRepository.model.json.PluginInfoBean
import org.jetbrains.intellij.pluginRepository.model.xml.PluginBean
import org.jetbrains.intellij.pluginRepository.model.xml.converters.convertCategory
import org.jetbrains.intellij.pluginRepository.utils.Messages
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.nio.file.Files
import java.time.Duration

private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")

/**
 * @param siteUrl url of plugins repository instance. For example: https://plugins.jetbrains.com
 * @param token hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
 */
class PluginRepositoryInstance(private val siteUrl: String, private val token: String? = null) {

  private val service = Retrofit.Builder()
    .baseUrl(siteUrl)
    .client(
      OkHttpClient()
        .newBuilder()
        .connectTimeout(Duration.ofMillis(10 * 60 * 1000))
        .addInterceptor { interceptor ->
          val request = if (token != null) interceptor.request()
            .newBuilder().addHeader("Authorization", "Bearer $token").build()
          else interceptor.request()
          interceptor.proceed(request)
        }
        .build()
    )
    .addConverterFactory(JaxbConverterFactory.create())
    .addConverterFactory(JacksonConverterFactory.create())
    .build()
    .create(PluginRepositoryService::class.java)


  fun listPlugins(ideBuild: String, channel: String?, pluginId: String?): List<PluginBean> {
    val response = getResponseOrNull(service.listPlugins(ideBuild, channel, pluginId))
    return response?.categories?.flatMap { convertCategory(it) } ?: emptyList()
  }

  fun pluginInfo(family: String, pluginXmlId: String): PluginInfoBean? {
    return getResponseOrNull(service.pluginInfo(family, pluginXmlId))
  }

  fun download(pluginXmlId: String, version: String, channel: String? = null, targetPath: String): File? {
    LOG.info("Downloading $pluginXmlId:$version")
    return getFileOrNull(service.download(pluginXmlId, version, channel), targetPath)
  }

  fun downloadCompatiblePlugin(
    pluginXmlId: String,
    ideBuild: String,
    channel: String? = null,
    targetPath: String
  ): File? {
    LOG.info("Downloading $pluginXmlId for $ideBuild build")
    return getFileOrNull(service.downloadCompatiblePlugin(pluginXmlId, ideBuild, channel), targetPath)
  }

  fun uploadNewPlugin(file: File, family: String, categoryId: Int, licenseUrl: String): PluginInfoBean {
    ensureCredentialsAreSet()
    LOG.info("Uploading new plugin from ${file.absolutePath}")
    val plugin = uploadOrFail(service.uploadNewPlugin(file, family, licenseUrl, categoryId.toString()))
    LOG.info("${plugin.name} was successfully uploaded with id ${plugin.id}")
    return plugin
  }

  @JvmOverloads
  fun uploadPlugin(pluginId: Int, file: File, channel: String? = null, notes: String? = null) {
    uploadPluginInternal(file, pluginId = pluginId, channel = channel, notes = notes)
  }

  @JvmOverloads
  fun uploadPlugin(pluginXmlId: String, file: File, channel: String? = null, notes: String? = null) {
    uploadPluginInternal(file, pluginXmlId = pluginXmlId, channel = channel, notes = notes)
  }

  private fun uploadPluginInternal(
    file: File,
    pluginId: Int? = null,
    pluginXmlId: String? = null,
    channel: String? = null,
    notes: String? = null
  ) {
    ensureCredentialsAreSet()
    val message = when {
      pluginXmlId != null -> uploadOrFail(service.uploadByXmlId(pluginXmlId, channel, notes, file.toRequestBody()), pluginXmlId)
      pluginId != null -> {
        val id = pluginId.toString()
        uploadOrFail(service.uploadByXmlId(id, channel, notes, file.toRequestBody()), id)
      }
      else -> restException(Messages.MISSING_PLUGINS_PARAMETERS)
    }
    LOG.info("Done: $message")
  }

  private fun ensureCredentialsAreSet() {
    if (token == null) throw RuntimeException(Messages.MISSION_TOKEN)
  }

  private fun downloadFile(executed: Response<ResponseBody>, targetPath: String): File? {
    val url = executed.raw().request().url().url().toExternalForm()
    val response = executed.body() ?: return null
    val mimeType = response.contentType()?.toString()
    if (mimeType != "application/zip" && mimeType != "application/java-archive") return null
    var targetFile = File(targetPath)
    if (targetFile.isDirectory) {
      val guessFileName = guessFileName(executed.raw(), url)
      if (guessFileName.contains(File.separatorChar)) {
        throw IOException(Messages.INVALID_FILENAME)
      }
      val file = File(targetFile, guessFileName)
      if (file.parentFile != targetFile) {
        throw IOException(Messages.INVALID_FILENAME)
      }
      targetFile = file
    }
    Files.copy(response.byteStream(), targetFile.toPath())
    LOG.info("Downloaded successfully to ${targetFile.absolutePath}")
    return targetFile
  }

  private fun guessFileName(response: okhttp3.Response, url: String): String {
    val filenameMarker = "filename="
    val contentDisposition = response.headers().names().find { it.equals("Content-Disposition", ignoreCase = true) }
                             ?: throw IOException(Messages.MISSING_CONTENT_DISPOSITION)
    val contentDispositionHeader = response.headers().get(contentDisposition)
    if (contentDispositionHeader == null || !contentDispositionHeader.contains(filenameMarker)) {
      val fileName = url.substringAfterLast('/')
      return if (fileName.isNotEmpty()) fileName else url
    }
    return contentDispositionHeader
      .substringAfter(filenameMarker, "")
      .substringBefore(';')
      .removeSurrounding("\"")
  }

  private fun <T> getResponseOrNull(callable: Call<T>): T? {
    return try {
      val executed = callable.execute()
      if (executed.isSuccessful) executed.body()
      else null
    }
    catch (e: Exception) {
      LOG.error(e.message, e)
      restException(e.message, e)
    }
  }

  private fun <T> uploadOrFail(callable: Call<T>, plugin: String? = null): T {
    return try {
      val executed = callable.execute()
      if (executed.isSuccessful) executed.body() ?: restException(Messages.FAILED_UPLOAD)
      else {
        val message = parseUploadErrorMessage(executed.errorBody(), executed.code(), plugin)
        restException(message)
      }
    }
    catch (e: Exception) {
      LOG.error(e.message, e)
      restException(e.message)
    }
  }

  private fun parseUploadErrorMessage(errorBody: ResponseBody?, code: Int, pluginName: String? = null): String {
    val error = errorBody ?: return Messages.FAILED_UPLOAD
    if (code == HttpURLConnection.HTTP_NOT_FOUND) return Messages.notFoundMessage(pluginName)
    val contextType = error.contentType()?.toString()
    return when {
      contextType?.startsWith("text/plain") == true -> error.string()
      contextType?.startsWith("application/json") == true -> {
        jacksonObjectMapper().readValue(error.string(), PluginUploadRestError::class.java).msg
      }
      else -> "${Messages.FAILED_UPLOAD} ${error.string()}"
    }
  }

  private fun getFileOrNull(callable: Call<ResponseBody>, targetPath: String): File? {
    return try {
      val executed = callable.execute()
      if (executed.isSuccessful) downloadFile(executed, targetPath) ?: return null
      else null
    }
    catch (e: Exception) {
      LOG.error(e.message, e)
      restException(e.message, e)
    }
  }

}

private fun File.toRequestBody() = RequestBody.create(MediaType.get("application/octet-stream"), this)