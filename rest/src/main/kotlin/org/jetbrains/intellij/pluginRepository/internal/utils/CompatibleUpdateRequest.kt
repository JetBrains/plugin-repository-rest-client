package org.jetbrains.intellij.pluginRepository.internal.utils

import org.jetbrains.intellij.pluginRepository.model.PluginXmlId

class CompatibleUpdateRequest(
  val pluginXMLIds: List<PluginXmlId>,
  val build: String,
  val channel: String
)