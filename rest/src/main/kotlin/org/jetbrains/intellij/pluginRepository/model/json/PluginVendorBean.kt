package org.jetbrains.intellij.pluginRepository.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginVendorBean(
  var name: String? = null,
  var url: String? = null
)