package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
enum class ProductEnum(
  val id: String,
  val title: String,
  val code: String,
  val alternativeCodes: List<String> = emptyList(),
  val edition: String? = null,
  val parent: ProductEnum? = null,
  val family: ProductFamily = ProductFamily.INTELLIJ,
  val alternativeIds: Set<String> = emptySet()
) {
  IDEA("idea", "IntelliJ IDEA", "IU", edition = "Ultimate", alternativeCodes = listOf("IIU")),
  IDEA_COMMUNITY("idea_ce", "IntelliJ IDEA", "IC", edition = "Community", parent = IDEA, alternativeCodes = listOf("IIC")),
  IDEA_EDUCATIONAL("idea_edu", "IntelliJ IDEA", "IE", edition = "Educational", parent = IDEA, alternativeCodes = listOf("IIE")),
  AQUA("aqua", "Aqua", code = "QA", alternativeCodes = listOf("TD"), alternativeIds = setOf("t_ide")),
  PHPSTORM("phpstorm", "PhpStorm", code = "PS"),
  WEBSTORM("webstorm", "WebStorm", code = "WS"),
  PYCHARM("pycharm", "PyCharm", "PY",  alternativeCodes = listOf("PYA", "PCP"), edition = "Professional"),
  PYCHARM_COMMUNITY("pycharm_ce", "PyCharm", "PC", alternativeCodes = listOf("PCA", "PCC"), edition = "Community", parent = PYCHARM),
  PYCHARM_EDUCATIONAL("pycharm_edu", "PyCharm", "PE", edition = "Educational", parent = PYCHARM, alternativeCodes = listOf("PCE")),
  DATASPELL("dataspell", "DataSpell", code = "PD", alternativeCodes = listOf("DS")),
  RUBYMINE("ruby", "RubyMine", "RM"),
  APPCODE("appcode", "AppCode", "OC", alternativeIds = setOf("objc"), alternativeCodes = listOf("AC")),
  CLION("clion", "CLion", code = "CL"),
  GOLAND("go", "GoLand", "GO"),
  DBE("dbe", "DataGrip", "DB", alternativeCodes = listOf("DG")),
  JBCLIENT("jbclient", "JetBrains Client", "JBC", alternativeCodes = listOf("JCD")),
  CWMGUEST("cwmguest", "Code With Me Guest", "CWMG", alternativeCodes = listOf("CWMR", "CWML")),
  GATEWAY("gateway", "JetBrains Gateway", "GW"),
  RIDER("rider", "Rider", "RD", alternativeCodes = listOf("RDCPPP")),
  RESHARPER("resharper", "ReSharper", "RS", family = ProductFamily.DOTNET, alternativeCodes = listOf("RSCLT", "RSCHB", "RC", "RSU")),
  MPS("mps", "MPS", code = "MPS"),
  ANDROID_STUDIO("androidstudio", "Android Studio", "AI"),
  TEAMCITY("teamcity", "TeamCity", "TC", family = ProductFamily.TEAMCITY),
  SPACE("space", "Space", "SP", family = ProductFamily.SPACE),
  FLEET("fleet", "Fleet", "FLEET", family = ProductFamily.FLEET),
  EDU_PLUGIN("edu_plugin", "Educational plugin", "EDU", family = ProductFamily.EDU),
  HUB("hub", "Hub", "HUB", family = ProductFamily.HUB, alternativeCodes = listOf("HB")),
  YOUTRACK("youtrack", "YouTrack", "YT", family = ProductFamily.HUB, alternativeCodes = listOf("YTD", "YTWE")),
  UPSOURCE("upsource", "Upsource", "UP", family = ProductFamily.HUB, alternativeCodes = listOf("US"));
}
