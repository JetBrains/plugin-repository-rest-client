package org.jetbrains.intellij.pluginRepository.internal.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.intellij.pluginRepository.PluginRepositoryException
import org.jetbrains.intellij.pluginRepository.internal.Messages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal fun <T> executeAndParseBody(callable: Call<T>): T? {
  val response = executeExceptionally(callable)
  if (response.isSuccessful) {
    return response.body()
  }
  val message = listOf(
    response.errorBody()?.string(),
    response.message(),
    response.raw().message,
    Messages.getMessage("failed.request.status.code", response.code())
  ).distinct().filterNot { it.isNullOrEmpty() }.joinToString("\n")
  throw PluginRepositoryException(message)
}

internal fun <T> executeExceptionally(callable: Call<T>): Response<T> {
  val responseRef = AtomicReference<Response<T>?>()
  val errorRef = AtomicReference<Throwable?>()
  val finished = AtomicBoolean()

  callable.enqueue(object : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
      responseRef.set(response)
      finished.set(true)
    }

    override fun onFailure(call: Call<T>, error: Throwable) {
      errorRef.set(error)
      finished.set(true)
    }
  })

  while (!finished.get()) {
    if (Thread.interrupted()) {
      callable.cancel()
      throw InterruptedException()
    }

    try {
      Thread.sleep(100)
    }
    catch (ie: InterruptedException) {
      callable.cancel()
      throw ie
    }
  }

  if (callable.isCanceled || Thread.interrupted()) {
    throw InterruptedException()
  }

  val error = errorRef.get()
  if (error != null) {
    throw error
  }
  return responseRef.get()!!
}

internal fun File.toMultipartBody(): MultipartBody.Part {
  val body = this.asRequestBody("application/octet-stream".toMediaType())
  return MultipartBody.Part.createFormData("file", this.name, body)
}

internal fun String.toRequestBody() = this.toRequestBody("text/plain".toMediaType())