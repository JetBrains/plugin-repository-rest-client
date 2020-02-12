package org.jetbrains.intellij.pluginRepository.internal.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import org.jetbrains.intellij.pluginRepository.*
import org.jetbrains.intellij.pluginRepository.internal.Messages
import org.jetbrains.intellij.pluginRepository.internal.instances.PluginDownloaderInstance
import org.jetbrains.intellij.pluginRepository.internal.instances.PluginManagerInstance
import org.jetbrains.intellij.pluginRepository.internal.instances.PluginUpdateManagerInstance
import org.jetbrains.intellij.pluginRepository.internal.instances.PluginUploaderInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


val LOG: Logger = LoggerFactory.getLogger("plugin-repository-rest-client")

internal class PluginRepositoryInstance(private val siteUrl: String, private val token: String? = null) : PluginRepository {

  private val maxParallelConnection = System.getProperty("MARKETPLACE_MAX_PARALLEL_CONNECTION", "16").toInt()

  private val dispatcher = Dispatcher().apply {
    this.maxRequestsPerHost = maxParallelConnection
    this.maxRequests = maxParallelConnection
  }

  private val service = Retrofit.Builder()
    .baseUrl(siteUrl)
    .client(
      OkHttpClient()
        .newBuilder()
        .dispatcher(Dispatcher(Executors.newCachedThreadPool(DaemonThreadFactory("retrofit-thread"))))
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .addInterceptor(object : Interceptor {
          override fun intercept(chain: Interceptor.Chain): Response {
            val request = if (token != null) {
              chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            }
            else {
              chain.request()
            }
            return chain.proceed(request)
          }
        })
        .dispatcher(dispatcher)
        .build())
    .addConverterFactory(JaxbConverterFactory.create())
    .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
    .build()
    .create(PluginRepositoryService::class.java)

  override val downloader: PluginDownloader = PluginDownloaderInstance(service)

  override val uploader: PluginUploader
    get() {
      ensureCredentialsAreSet()
      return PluginUploaderInstance(service)
    }
  override val pluginManager: PluginManager = PluginManagerInstance(service)

  override val pluginUpdateManager: PluginUpdateManager = PluginUpdateManagerInstance(service)

  private fun ensureCredentialsAreSet() {
    if (token == null) throw RuntimeException(Messages.getMessage("missing.token"))
  }

  private class DaemonThreadFactory(private val threadNamePrefix: String) : ThreadFactory {

    private val defaultThreadFactory = Executors.defaultThreadFactory()

    private val nameCount = AtomicLong()

    override fun newThread(r: Runnable): Thread = defaultThreadFactory.newThread(r).apply {
      name = "$threadNamePrefix-${nameCount.getAndIncrement()}"
      isDaemon = true
    }
  }
}