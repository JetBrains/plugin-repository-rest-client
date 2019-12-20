package org.jetbrains.intellij.pluginRepository.exceptions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginUploadRestError(
  @Deprecated("No longer support in Marketplace REST API", replaceWith = ReplaceWith("message"), level = DeprecationLevel.WARNING)
  val msg: String? = null,
  val message: String? = null
)