package org.jetbrains.intellij.pluginRepository.base

import org.jetbrains.intellij.pluginRepository.PluginRepositoryFactory

val TOKEN = System.getProperty("jetbrains.plugin.repository.token") ?: null
val REPOSITORY_HOST = System.getProperty("jetbrains.plugin.repository.host") ?: "https://plugins.jetbrains.com"

open class BaseTest {
  companion object {
    val INSTANCE = PluginRepositoryFactory.create(REPOSITORY_HOST, TOKEN)
    val NON_AUTH_INSTANCE = PluginRepositoryFactory.create(REPOSITORY_HOST, null)
  }
}
