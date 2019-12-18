package org.jetbrains.intellij.pluginRepository.model.xml

import javax.xml.bind.annotation.*

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlCategoryBean(
  @field:XmlAttribute var name: String? = null,
  @field:XmlElement(name = "idea-plugin") var plugins: List<XmlPluginBean>? = null
)