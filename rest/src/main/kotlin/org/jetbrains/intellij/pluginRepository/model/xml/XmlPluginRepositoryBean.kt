package org.jetbrains.intellij.pluginRepository.model.xml

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlPluginRepositoryBean(
  @field:XmlElement(name = "category") var categories: List<XmlCategoryBean>? = null
)