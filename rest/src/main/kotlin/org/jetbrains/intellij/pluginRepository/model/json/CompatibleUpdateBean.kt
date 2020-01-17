package org.jetbrains.intellij.pluginRepository.model.json

data class CompatibleUpdateBean(
  val id: Int,
  val pluginId: Int,
  val pluginXmlId: Int,
  val version: String
)