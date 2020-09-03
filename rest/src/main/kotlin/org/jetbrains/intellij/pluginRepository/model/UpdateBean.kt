package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateBean(
  val id: UpdateId,
  val pluginId: PluginId,
  val pluginXmlId: PluginXmlId,
  val version: String,
  val channel: String?
)
