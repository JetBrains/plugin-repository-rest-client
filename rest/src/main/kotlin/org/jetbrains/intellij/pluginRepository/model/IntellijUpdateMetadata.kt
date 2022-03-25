package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class IntellijUpdateMetadata(
  val id: UpdateId,
  val xmlId: StringPluginId,
  val name: String,
  val description: String,
  val tags: List<String> = listOf(),
  val vendor: String = "",
  val version: String = "",
  val notes: String = "",
  val dependencies: Set<String> = emptySet(),
  val optionalDependencies: Set<String> = emptySet(),
  val since: String? = null,
  val until: String? = null,
  val productCode: String? = null,
  val sourceCodeUrl: String? = null,
  val url: String? = null,
  val size: Int = 0
)

