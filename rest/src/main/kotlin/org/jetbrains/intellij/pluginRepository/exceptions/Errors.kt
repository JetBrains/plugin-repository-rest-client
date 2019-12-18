package org.jetbrains.intellij.pluginRepository.exceptions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginUploadRestError(val msg: String = "")