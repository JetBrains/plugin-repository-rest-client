package org.jetbrains.intellij.pluginRepository.model

data class PluginUrlsForm(
  val urls: PluginUrls?,
)

data class PluginUrls(
  val forumUrl: String? = null,
  val privacyPolicyUrl: String? = null,
  val bugtrackerUrl: String? = null,
  val docUrl: String? = null,
  val sourceCodeUrl: String? = null,
  val videoUrl: String? = null,
  val donationLinks: List<DonationInfoBean>? = null,
  val customContacts: List<CustomContactBean>? = null
)
