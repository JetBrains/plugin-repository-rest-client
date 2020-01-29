package org.jetbrains.intellij.pluginRepository.model

data class PluginXmlBean(
  val name: String,
  val id: PluginXmlId,
  val version: String,
  val category: String,
  val sinceBuild: String?,
  val untilBuild: String?,
  val vendor: String?,
  val depends: List<String>
)