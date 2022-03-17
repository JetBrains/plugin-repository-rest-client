package org.jetbrains.intellij.pluginRepository

import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryInstance
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService

object PluginRepositoryFactory {

  private const val DEFAULT_HOST = "https://plugins.jetbrains.com"
  private const val DEFAULT_AUTH_SCHEME = "Bearer"

  /**
   * Factory object to create new object of [PluginRepository] interface
   * @see PluginRepository
   * @param host       - url of plugins repository instance. Example: https://plugins.jetbrains.com
   * @param token      - hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
   * @param authScheme - authorization scheme. Example: Bearer
   */
  @JvmStatic
  @JvmOverloads
  fun create(
    host: String = DEFAULT_HOST,
    token: String? = null,
    authScheme: String = DEFAULT_AUTH_SCHEME,
  ): PluginRepository = createWithImplementationClass(host, token, authScheme, PluginRepositoryService::class.java)

  /**
   * Factory object to create new object of [PluginRepository] interface with a custom [PluginRepositoryService]
   * implementation class.
   * @see PluginRepository
   * @see PluginRepositoryService
   * @param host         - url of plugins repository instance. Example: https://plugins.jetbrains.com
   * @param token        - hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
   * @param authScheme   - authorization scheme. Example: Bearer
   * @param serviceClass - interface for building the Retrofit service. Example PluginRepositoryService::class.java
   */
  @JvmStatic
  @JvmOverloads
  fun <T : PluginRepositoryService> createWithImplementationClass(
    host: String = DEFAULT_HOST,
    token: String? = null,
    authScheme: String = DEFAULT_AUTH_SCHEME,
    serviceClass: Class<T>,
  ): PluginRepository = PluginRepositoryInstance(host, token, authScheme, serviceClass)
}
