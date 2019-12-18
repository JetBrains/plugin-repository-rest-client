package org.jetbrains.intellij.pluginRepository.model.xml

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlPluginBean(
  @param:XmlElement(name = "name")
  @field:XmlElement
  val name: String = "",

  @param:XmlElement(name = "id")
  @field:XmlElement
  val id: String = "",

  @param:XmlElement(name = "version")
  @field:XmlElement
  val version: String = "",

  @param:XmlElement(name = "idea-version")
  @field:XmlElement(name = "idea-version")
  val ideaVersion: XmlIdeaVersionBean? = null,

  @param:XmlElement(name = "vendor", required = false)
  @field:XmlElement(required = false)
  val vendor: String? = null,

  @param:XmlElement(name = "depends", required = false)
  @field:XmlElement(name = "depends", required = false)
  val depends: List<String>? = null
)