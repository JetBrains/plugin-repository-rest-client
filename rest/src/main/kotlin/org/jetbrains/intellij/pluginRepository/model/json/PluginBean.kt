package org.jetbrains.intellij.pluginRepository.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.jetbrains.intellij.pluginRepository.model.repository.ProductFamily

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginBean(
  val id: Int,
  val name: String,
  val link: String,
  val approve: Boolean,
  val xmlId: String,
  val description: String?,
  val customIdeList: Boolean,
  val preview: String?,
  val docText: String?,
  val email: String?,
  val family: ProductFamily,
  val copyright: String?,
  val downloads: Int,
  val purchaseInfo: PluginPurchaseInfoBean?,
  val vendor: PluginVendorBean?,
  val urls: PluginURLsBean,
  val tags: List<PluginTagBean>?,
  val removalRequested: Boolean?,
  val hasUnapprovedUpdate: Boolean?,
  val readyForSale: Boolean,
  val screenshots: List<String>?,
  val themes: Set<IntellijThemeBean>?,
  val icon: String?
)

data class IntellijThemeBean(val name: String, val dark: Boolean)

data class PluginPurchaseInfoBean(val productCode: String, val buyUrl: String?, val purchaseTerms: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginTagBean(
  val id: Int,
  val name: String,
  val link: String,
  val families: List<ProductFamily>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginURLsBean(
  val url: String?,
  val forumUrl: String?,
  val licenseUrl: String?,
  val bugtrackerUrl: String?,
  val docUrl: String?,
  val sourceCodeUrl: String?
)