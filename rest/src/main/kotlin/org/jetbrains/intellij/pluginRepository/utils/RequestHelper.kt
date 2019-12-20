package org.jetbrains.intellij.pluginRepository.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.LOG
import org.jetbrains.intellij.pluginRepository.exceptions.PluginUploadRestError
import org.jetbrains.intellij.pluginRepository.exceptions.restException
import org.jetbrains.intellij.pluginRepository.exceptions.uploadException
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.net.HttpURLConnection

internal fun <T> getResponseOrNull(callable: Call<T>): T? = executeAndCall(callable) { it.body() }

internal fun getFileOrNull(callable: Call<ResponseBody>, targetPath: String): File? = executeAndCall(callable) {
  downloadFile(it, targetPath)
}

internal fun <T> uploadOrFail(callable: Call<T>, plugin: String? = null): T {
  return try {
    val executed = callable.execute()
    if (executed.isSuccessful) executed.body() ?: restException(Messages.FAILED_UPLOAD)
    else {
      val message = parseUploadErrorMessage(executed.errorBody(), executed.code(), plugin)
      uploadException(message)
    }
  }
  catch (e: Exception) {
    LOG.error(e.message, e)
    uploadException(e.message)
  }
}

private fun <T, R> executeAndCall(callable: Call<T>, block: (response: Response<T>) -> R): R? {
  return try {
    val executed = callable.execute()
    if (executed.isSuccessful) block(executed)
    else null
  }
  catch (e: Exception) {
    LOG.error(e.message, e)
    restException(e.message, e)
  }
}


private fun parseUploadErrorMessage(errorBody: ResponseBody?, code: Int, pluginName: String? = null): String {
  val error = errorBody ?: return Messages.FAILED_UPLOAD
  if (code == HttpURLConnection.HTTP_NOT_FOUND) return Messages.notFoundMessage(pluginName)
  val contextType = error.contentType()?.toString()
  return when {
    contextType?.startsWith("text/plain") == true -> error.string()
    contextType?.startsWith("application/json") == true -> {
      val restError = jacksonObjectMapper().readValue(error.string(), PluginUploadRestError::class.java)
      if (restError.msg != null) return restError.msg else restError.message ?: Messages.FAILED_UPLOAD
    }
    else -> "${Messages.FAILED_UPLOAD} ${error.string()}"
  }
}