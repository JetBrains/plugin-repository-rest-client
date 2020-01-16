package org.jetbrains.intellij.pluginRepository.internal.utils

import org.jetbrains.intellij.pluginRepository.PluginRepositoryException
import org.jetbrains.intellij.pluginRepository.internal.Messages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal fun <T> executeAndParseBody(callable: Call<T>): T? {
  val response = executeExceptionally(callable)
  if (response.isSuccessful) {
    return response.body()
  }
  val message = response.errorBody()?.string() ?: response.message() ?: Messages.getMessage("failed.request.status.code", response.code())
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
    } catch (ie: InterruptedException) {
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