package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
enum class ProductEnum(
  val id: String,
  val title: String,
  val logo: String = id,
  val code: String,
  val alternativeCodes: List<String> = emptyList(),
  val edition: String? = null,
  val parent: ProductEnum? = null,
  val family: ProductFamily = ProductFamily.INTELLIJ
) {
  IDEA("idea", "IntelliJ IDEA", "intellij-idea", "IU", edition = "Ultimate"),
  IDEA_COMMUNITY("idea_ce", "IntelliJ IDEA", "intellij-idea", "IC", edition = "Community", parent = IDEA),
  IDEA_EDUCATIONAL("idea_edu", "IntelliJ IDEA", "intellij-idea", "IE", edition = "Educational", parent = IDEA),
  TIDE("t_ide", "T-IDE", code = "TD"),
  PHPSTORM("phpstorm", "PhpStorm", code = "PS"),
  WEBSTORM("webstorm", "WebStorm", code = "WS"),
  PYCHARM("pycharm", "PyCharm", "pycharm", "PY", alternativeCodes = listOf("PYA"), edition = "Professional"),
  PYCHARM_COMMUNITY("pycharm_ce", "PyCharm", "pycharm", "PC", alternativeCodes = listOf("PCA"), edition = "Community", parent = PYCHARM),
  PYCHARM_EDUCATIONAL("pycharm_edu", "PyCharm", "pycharm-edu", "PE", edition = "Educational", parent = PYCHARM),
  RUBYMINE("ruby", "RubyMine", "rubymine", "RM"),
  APPCODE("objc", "AppCode", "appcode", "OC"),
  CLION("clion", "CLion", code = "CL"),
  GOLAND("go", "GoLand", "gogland", "GO"),
  DBE("dbe", "DataGrip", "datagrip", "DB"),
  GATEWAY("gateway", "JetBrains Gateway", code = "CWMG"),
  RIDER("rider", "Rider", code = "RD", alternativeCodes = listOf("RDCPPP")),
  RESHARPER("resharper", "ReSharper", "resharper", "RS", family = ProductFamily.DOTNET),
  MPS("mps", "MPS", code = "MPS"),
  ANDROID_STUDIO("androidstudio", "Android Studio", code = "AI"),
  TEAMCITY("teamcity", "TeamCity", "teamcity", "TC", family = ProductFamily.TEAMCITY),
  EDU_PLUGIN("edu_plugin", "Educational plugin", "edu", "EDU", family = ProductFamily.EDU),
  HUB("hub", "Hub", "hub", "HUB", family = ProductFamily.HUB),
  YOUTRACK("youtrack", "YouTrack", "youtrack", "YT", family = ProductFamily.HUB),
  UPSOURCE("upsource", "Upsource", "upsource", "UP", family = ProductFamily.HUB),
  @JsonEnumDefaultValue UNKNOWN("UNKNOWN", "UNKNOWN", code = "UNKNOWN");
}
