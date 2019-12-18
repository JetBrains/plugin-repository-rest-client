package org.jetbrains.intellij.pluginRepository.model.xml

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlIdeaVersionBean(
  @field:XmlAttribute(name = "since-build", required = false) var sinceBuild: String? = null,
  @field:XmlAttribute(name = "until-build", required = false) var untilBuild: String? = null
)