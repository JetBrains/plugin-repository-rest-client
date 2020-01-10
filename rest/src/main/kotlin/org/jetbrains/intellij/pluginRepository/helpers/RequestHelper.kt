package org.jetbrains.intellij.pluginRepository.helpers

import org.jetbrains.intellij.pluginRepository.exceptions.PluginRepositoryException
import org.jetbrains.intellij.pluginRepository.utils.Messages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal fun <T> executeAndParseBody(callable: Call<T>): T = getBodyOrThrow(callable) { response ->
  val message = response.errorBody()?.string() ?: response.message() ?: "Failed request"
  throw PluginRepositoryException(message)
}

internal fun <T> getBodyOrThrow(callable: Call<T>, throwable: (response: Response<T>) -> T): T {
  val (response, error) = executeWithInterruptionCheck(callable)
  if (error != null) throw error
  if (response.isSuccessful) {
    return response.body() ?: throw PluginRepositoryException(Messages.getMessage("not.response.provided"))
  }
  return throwable(response)
}

internal fun <T> executeWithInterruptionCheck(callable: Call<T>): Pair<Response<T>, Throwable?> {
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

  val response = responseRef.get() ?: throw PluginRepositoryException(Messages.getMessage("not.response.provided"))
  val error = errorRef.get()
  return response to error
}