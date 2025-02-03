package org.jetbrains.intellij.pluginRepository.internal.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.jetbrains.intellij.pluginRepository.*
import org.jetbrains.intellij.pluginRepository.internal.Messages
import org.jetbrains.intellij.pluginRepository.internal.instances.*
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

internal class PluginRepositoryInstance<T : PluginRepositoryService>(
  siteUrl: String,
  private val token: String? = null,
  private val authScheme: String,
  serviceClass: Class<T>,
) : PluginRepository {
  companion object {
    private val version = PluginRepository::class.java.`package`.implementationVersion ?: "SNAPSHOT"
  }

  private val maxParallelConnection = System.getProperty("MARKETPLACE_MAX_PARALLEL_CONNECTIONS", "16").toInt()

  private val executorService = Executors.newFixedThreadPool(maxParallelConnection, DaemonThreadFactory("retrofit-thread"))

  private val dispatcher = Dispatcher(executorService).apply {
    this.maxRequestsPerHost = maxParallelConnection
    this.maxRequests = maxParallelConnection
  }

  private val okHttpClient = OkHttpClient()
    .newBuilder()
    .dispatcher(dispatcher)
    .connectTimeout(5, TimeUnit.MINUTES)
    .readTimeout(5, TimeUnit.MINUTES)
    .writeTimeout(5, TimeUnit.MINUTES)
    .addInterceptor(Interceptor { chain ->
      val requestBuilder = chain
        .request()
        .newBuilder()
        .addHeader("User-Agent", "marketplace-rest-client/$version")
      if (token != null) {
        requestBuilder.addHeader("Authorization", "$authScheme $token")
      }
      chain.proceed(requestBuilder.build())
    })
    .build()

  private val service = Retrofit.Builder()
    .baseUrl(siteUrl)
    .client(okHttpClient)
    .addConverterFactory(JaxbConverterFactory.create())
    .addConverterFactory(JacksonConverterFactory.create(
      jacksonObjectMapper()
        .configure(
          DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true
        )
    ))
    .build()
    .create(serviceClass)

  init {
    Runtime.getRuntime().addShutdownHook(Thread {
      dispatcher.executorService.shutdownNow()
      okHttpClient.connectionPool.evictAll()
      okHttpClient.cache?.close()
    })
  }

  override val downloader: PluginDownloader = PluginDownloaderInstance(service)

  override val uploader: PluginUploader
    get() {
      ensureCredentialsAreSet()
      return PluginUploaderInstance(service)
    }
  override val pluginManager: PluginManager = PluginManagerInstance(service)

  override val pluginUpdateManager: PluginUpdateManager = PluginUpdateManagerInstance(service)

  override val vendorManager: VendorManager = VendorManagerInstance(service)

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
