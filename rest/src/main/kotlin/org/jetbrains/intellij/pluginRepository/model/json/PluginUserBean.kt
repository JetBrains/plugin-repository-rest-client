package org.jetbrains.intellij.pluginRepository.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class PluginUserBean(
  val id: String,
  val name: String,
  val link: String? = null,
  val hubLogin: String? = null
)