package org.jetbrains.intellij.pluginRepository.base

import org.jetbrains.intellij.pluginRepository.PluginRepositoryInstance

val TOKEN = System.getProperty("PLUGIN_TOKEN") ?: null
val REPOSITORY_HOST = System.getProperty("REPOSITORY_HOST") ?: "https://plugins.jetbrains.com"

open class BaseTest {
  val instance = PluginRepositoryInstance(REPOSITORY_HOST, TOKEN)
}