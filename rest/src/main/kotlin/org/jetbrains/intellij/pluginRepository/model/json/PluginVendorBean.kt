package org.jetbrains.intellij.pluginRepository.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginVendorBean(val name: String, val url: String?)