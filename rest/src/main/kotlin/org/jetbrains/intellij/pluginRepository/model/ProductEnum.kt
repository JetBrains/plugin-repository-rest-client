package org.jetbrains.intellij.pluginRepository.model

enum class ProductEnum(
  val id: String,
  val code: String,
  val title: String,
  val logo: String = id,
  val edition: String? = null,
  val parent: ProductEnum? = null,
  val family: ProductFamily = ProductFamily.INTELLIJ
) {
  IDEA("idea", "IU", "IntelliJ IDEA", "intellij-idea", "Ultimate"),
  IDEA_COMMUNITY("idea_ce", "IC", "IntelliJ IDEA", "intellij-idea", "Community", IDEA),
  IDEA_EDUCATIONAL("idea_edu", "IE", "IntelliJ IDEA", "intellij-idea", "Educational", IDEA),
  PHPSTORM("phpstorm", "PS", "PhpStorm"),
  WEBSTORM("webstorm", "WS", "WebStorm"),
  PYCHARM("pycharm", "PY", "PyCharm", "pycharm", "Professional"),
  PYCHARM_COMMUNITY("pycharm_ce", "PC", "PyCharm", "pycharm", "Community", PYCHARM),
  PYCHARM_EDUCATIONAL("pycharm_edu", "PE", "PyCharm", "pycharm-edu", "Educational", PYCHARM),
  RUBYMINE("ruby", "RM", "RubyMine", "rubymine"),
  APPCODE("objc", "OC", "AppCode", "appcode"),
  CLION("clion", "CL", "CLion"),
  GOLAND("go", "GO", "GoLand", "gogland"),
  DBE("dbe", "DB", "DataGrip", "datagrip"),
  RIDER("rider", "RD", "Rider"),
  RESHARPER("resharper", "RS", "ReSharper", "resharper", family = ProductFamily.DOTNET),
  MPS("mps", "MPS", "MPS"),
  ANDROID_STUDIO("androidstudio", "AI", "Android Studio"),
  TEAMCITY("teamcity", "TC", "TeamCity", "teamcity", family = ProductFamily.TEAMCITY),
  HUB("hub", "HUB", "Hub", "hub", family = ProductFamily.HUB),
  YOUTRACK("youtrack", "YT", "YouTrack", "youtrack", family = ProductFamily.HUB),
  UPSOURCE("upsource", "UP", "Upsource", "upsource", family = ProductFamily.HUB);
}