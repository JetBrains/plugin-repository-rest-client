package org.jetbrains.intellij.pluginRepository.model

data class UpdateBean(
  val id: UpdateId,
  val pluginId: PluginId,
  val pluginXmlId: PluginXmlId,
  val version: String,
  val channel: String?
)
