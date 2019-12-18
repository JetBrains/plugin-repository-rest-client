package org.jetbrains.intellij.pluginRepository.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginInfoBean(
  val id: String = "",
  val name: String = "",
  val vendor: PluginVendorBean? = null
)