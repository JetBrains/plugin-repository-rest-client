package org.jetbrains.intellij.pluginRepository.internal.utils

class CompatibleUpdateRequest(
  val pluginXMLIds: List<String>,
  val build: String,
  val channel: String
)