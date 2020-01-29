package org.jetbrains.intellij.pluginRepository.model

data class UpdateBean(
  val id: Int,
  val pluginId: Int,
  val pluginXmlId: String,
  val version: String
)