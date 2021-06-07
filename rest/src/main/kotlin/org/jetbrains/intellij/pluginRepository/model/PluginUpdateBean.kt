package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginUpdateBean(
  val id: UpdateId,
  val version: String?,
  val cdate: String?,
  val downloadUrl: String?,
  val notes: String?,
  val since: String?,
  val until: String?,
  val sinceUntil: String?,
  val channel: String?,
  val size: Int?,
  val downloads: Int?,
  val pluginId: PluginId,
  val compatibleVersions: Map<ProductEnum, String>? = null,
  val author: PluginUserBean?,
  val modules: Set<String>?
)
