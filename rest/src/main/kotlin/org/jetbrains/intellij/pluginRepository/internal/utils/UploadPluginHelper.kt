package org.jetbrains.intellij.pluginRepository.internal.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.PluginRepositoryException
import org.jetbrains.intellij.pluginRepository.internal.Messages
import retrofit2.Call
import java.net.HttpURLConnection

internal fun <T> uploadOrFail(callable: Call<T>, plugin: String? = null): T {
  val response = executeExceptionally(callable)
  if (response.isSuccessful) {
    return response.body() ?: throw PluginRepositoryException(Messages.getMessage("no.response.from.server"))
  }
  val message = parseUploadErrorMessage(response.errorBody(), response.code(), plugin)
  throw PluginRepositoryException("Upload failed: $message")
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PluginUploadRestError(
  @Deprecated("No longer support in Marketplace REST API", replaceWith = ReplaceWith("message"), level = DeprecationLevel.WARNING)
  val msg: String? = null,
  val message: String? = null
)

private fun parseUploadErrorMessage(errorBody: ResponseBody?, code: Int, pluginName: String? = null): String {
  val error = errorBody ?: return Messages.getMessage("failed.upload", code)
  if (code == HttpURLConnection.HTTP_NOT_FOUND) return Messages.getMessage("not.found", pluginName ?: "plugin")
  val contextType = error.contentType()?.toString()
  return when {
    contextType?.startsWith("text/plain") == true -> error.string()
    contextType?.startsWith("application/json") == true -> {
      val restError = jacksonObjectMapper().readValue(error.string(), PluginUploadRestError::class.java)
      @Suppress("DEPRECATION")
      restError.msg ?: (restError.message ?: Messages.getMessage("failed.upload", code))
    }
    else -> "${Messages.getMessage("failed.upload", code)} ${error.string()}"
  }
}
