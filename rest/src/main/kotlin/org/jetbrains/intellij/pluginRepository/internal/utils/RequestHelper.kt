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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

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

internal fun <T> executeExceptionallyBatch(calls: List<Call<T>>): Map<Call<T>, Response<T>> {
  val responses = ConcurrentHashMap<Call<T>, Response<T>>()
  val errors = ConcurrentHashMap<Call<T>, Throwable>()

  val finished = AtomicInteger()

  for (call in calls) {
    call.enqueue(object : Callback<T> {
      override fun onResponse(call: Call<T>, response: Response<T>) {
        finished.incrementAndGet()
        responses[call] = response
      }

      override fun onFailure(call: Call<T>, error: Throwable) {
        finished.incrementAndGet()
        errors[call] = error
      }
    })
  }

  while (finished.get() != calls.size) {
    if (Thread.interrupted()) {
      for (call in calls) {
        call.cancel()
      }
      throw InterruptedException()
    }

    try {
      Thread.sleep(100)
    }
    catch (ie: InterruptedException) {
      for (call in calls) {
        call.cancel()
      }
      throw ie
    }
  }

  if (Thread.interrupted()) {
    throw InterruptedException()
  }

  if (errors.isNotEmpty()) {
    val exception = Exception()
    for (error in errors.values) {
      exception.addSuppressed(error)
    }
    throw exception
  }
  return responses
}

internal fun <T> executeExceptionally(call: Call<T>): Response<T> =
  executeExceptionallyBatch(listOf(call)).values.first()

internal fun File.toMultipartBody(): MultipartBody.Part {
  val body = this.asRequestBody("application/octet-stream".toMediaType())
  return MultipartBody.Part.createFormData("file", this.name, body)
}

internal fun String.toRequestBody() = this.toRequestBody("text/plain".toMediaType())