package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginVendorBean(
  val name: String = "",
  val url: String? = null,
  val link: String = "",
  val publicName: String = "",
)
