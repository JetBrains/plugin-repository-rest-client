package org.jetbrains.intellij.pluginRepository.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.exceptions.PluginRepositoryException
import org.jetbrains.intellij.pluginRepository.exceptions.UploadFailedException
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection

internal fun <T> getResponseOrNull(callable: Call<T>): T? = executeAndCall(callable) { it.body() }

internal fun <T> uploadOrFail(callable: Call<T>, plugin: String? = null): T {
  return try {
    val executed = callable.execute()
    if (executed.isSuccessful) executed.body() ?: throw UploadFailedException(Messages.getMessage("failed.upload"), null)
    else {
      val message = parseUploadErrorMessage(executed.errorBody(), executed.code(), plugin)
      throw UploadFailedException(message, null)
    }
  }
  catch (e: Exception) {
    throw UploadFailedException(e.message, e)
  }
}

internal fun <T, R> executeAndCall(callable: Call<T>, block: (response: Response<T>) -> R): R? {
  return try {
    val executed = callable.execute()
    if (executed.isSuccessful) block(executed)
    else null
  }
  catch (e: Exception) {
    throw PluginRepositoryException(e.message, e)
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PluginUploadRestError(
    @Deprecated("No longer support in Marketplace REST API", replaceWith = ReplaceWith("message"), level = DeprecationLevel.WARNING)
    val msg: String? = null,
    val message: String? = null
)

private fun parseUploadErrorMessage(errorBody: ResponseBody?, code: Int, pluginName: String? = null): String {
  val error = errorBody ?: return Messages.getMessage("failed.upload")
  if (code == HttpURLConnection.HTTP_NOT_FOUND) return Messages.getMessage("not.found", pluginName ?: "plugin")
  val contextType = error.contentType()?.toString()
  return when {
    contextType?.startsWith("text/plain") == true -> error.string()
    contextType?.startsWith("application/json") == true -> {
      val restError = jacksonObjectMapper().readValue(error.string(), PluginUploadRestError::class.java)
      @Suppress("DEPRECATION")
      if (restError.msg != null) return restError.msg else restError.message ?: Messages.getMessage("failed.upload")
    }
    else -> "${Messages.getMessage("failed.upload")} ${error.string()}"
  }
}