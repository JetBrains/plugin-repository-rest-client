package org.jetbrains.intellij.pluginRepository.internal.utils

import org.jetbrains.intellij.pluginRepository.model.PluginXmlBean
import org.jetbrains.intellij.pluginRepository.model.XmlCategoryBean
import org.jetbrains.intellij.pluginRepository.model.XmlPluginBean

internal fun convertCategory(response: XmlCategoryBean): List<PluginXmlBean> {
  return response.plugins?.map {
    convertPlugin(it, response.name!!)
  } ?: emptyList()
}

private fun convertPlugin(response: XmlPluginBean, category: String) = PluginXmlBean(
  name = response.name,
  id = response.id,
  version = response.version,
  category = category,
  sinceBuild = response.ideaVersion?.sinceBuild,
  untilBuild = response.ideaVersion?.untilBuild,
  vendor = response.vendor,
  depends = response.depends ?: emptyList()
)