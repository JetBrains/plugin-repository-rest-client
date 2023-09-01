package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginVendorBean(
  val id: Int?,
  val name: String,
  val url: String?,
  val link: String,
  val publicName: String,
  val email: String?,
  val countryCode: String,
  val country: String?,
  @get:JsonProperty(value = "isVerified")
  val isVerified: Boolean,
  @get:JsonProperty(value = "isTrader")
  val isTrader: Boolean?,
  val description: String?,
  val type: VendorType
)

enum class VendorType { organization, personal, xml }

