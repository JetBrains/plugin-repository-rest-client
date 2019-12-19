package org.jetbrains.intellij.pluginRepository.model.xml

import javax.xml.bind.annotation.*

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlPluginRepositoryBean(
  @field:XmlElement(name = "category") var categories: List<XmlCategoryBean>? = null
)

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlCategoryBean(
  @field:XmlAttribute var name: String? = null,
  @field:XmlElement(name = "idea-plugin") var plugins: List<XmlPluginBean>? = null
)

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

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class XmlIdeaVersionBean(
  @field:XmlAttribute(name = "since-build", required = false) var sinceBuild: String? = null,
  @field:XmlAttribute(name = "until-build", required = false) var untilBuild: String? = null
)