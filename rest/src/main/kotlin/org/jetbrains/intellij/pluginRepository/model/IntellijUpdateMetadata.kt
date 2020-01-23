package org.jetbrains.intellij.pluginRepository.model


data class IntellijUpdateMetadata(
  val id: Int,
  val xmlId: String,
  val name: String,
  val description: String,
  val tags: List<String>,
  val vendor: String,
  val version: String,
  val notes: String,
  val dependencies: Set<String>,
  val since: String,
  val until: String,
  val productCode: String?
)

