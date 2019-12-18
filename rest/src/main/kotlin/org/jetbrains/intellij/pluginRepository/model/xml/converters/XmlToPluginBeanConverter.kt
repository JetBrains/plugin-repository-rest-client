package org.jetbrains.intellij.pluginRepository.model.xml.converters

import org.jetbrains.intellij.pluginRepository.model.xml.PluginBean
import org.jetbrains.intellij.pluginRepository.model.xml.XmlCategoryBean
import org.jetbrains.intellij.pluginRepository.model.xml.XmlPluginBean

internal fun convertCategory(response: XmlCategoryBean): List<PluginBean> {
  return response.plugins?.map { convertPlugin(it, response.name!!) } ?: emptyList()
}

private fun convertPlugin(response: XmlPluginBean, category: String): PluginBean {
  return PluginBean(
    name = response.name,
    id = response.id,
    version = response.version,
    category = category,
    sinceBuild = response.ideaVersion?.sinceBuild,
    untilBuild = response.ideaVersion?.untilBuild,
    vendor = response.vendor,
    depends = response.depends ?: emptyList()
  )
}