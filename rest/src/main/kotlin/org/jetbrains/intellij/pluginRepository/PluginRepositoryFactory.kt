package org.jetbrains.intellij.pluginRepository

import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryInstance

object PluginRepositoryFactory {

  private const val DEFAULT_HOST = "https://plugins.jetbrains.com"
  /**
   * Factory object to create new object of [PluginRepository] interface
   * @see PluginRepository
   * @param host  - url of plugins repository instance. Example: https://plugins.jetbrains.com
   * @param token - hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
   */
  @JvmStatic
  @JvmOverloads
  fun create(host: String = DEFAULT_HOST, token: String? = null): PluginRepository = PluginRepositoryInstance(host, token)
}