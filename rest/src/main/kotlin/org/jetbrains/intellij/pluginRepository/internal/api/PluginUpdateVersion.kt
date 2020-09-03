package org.jetbrains.intellij.pluginRepository.internal.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginUpdateVersion(val id: Int, val version: String, val channel: String?)
