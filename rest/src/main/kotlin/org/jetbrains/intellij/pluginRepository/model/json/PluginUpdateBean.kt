package org.jetbrains.intellij.pluginRepository.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.jetbrains.intellij.pluginRepository.model.repository.ProductEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginUpdateBean(
  val id: Int,
  val link: String,
  val version: String?,
  val cdate: String?,
  val downloadUrl: String?,
  val file: String?,
  val notes: String?,
  val since: String?,
  val until: String?,
  val sinceUntil: String?,
  val channel: String?,
  val size: Int?,
  val downloads: Int?,
  val pluginId: Int,
  val compatibleVersions: Map<ProductEnum, String>?,
  val author: PluginUserBean?,
  val modules: Set<String>?
)