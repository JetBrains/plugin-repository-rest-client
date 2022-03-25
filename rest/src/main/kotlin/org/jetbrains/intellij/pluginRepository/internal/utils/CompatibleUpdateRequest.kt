package org.jetbrains.intellij.pluginRepository.internal.utils

import org.jetbrains.intellij.pluginRepository.model.StringPluginId

class CompatibleUpdateRequest(
  val pluginXMLIds: List<StringPluginId>,
  val build: String,
  val channel: String,
  val module: String
)
