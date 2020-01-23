package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class PluginUserBean(
  val id: String,
  val name: String,
  val link: String,
  val hubLogin: String
)