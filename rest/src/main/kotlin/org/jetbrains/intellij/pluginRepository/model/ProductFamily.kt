package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonValue

enum class ProductFamily(@JsonValue val id: String) {
  TEAMCITY("teamcity"),
  TEAMCITY_ACTIONS("teamcity_actions"),
  INTELLIJ("intellij"),
  HUB("hub"),
  EDU("edu"),
  FLEET("fleet"),
  TOOLBOX("toolbox"),
  SPACE("space"),
  DOTNET("dotnet"),
  YOUTRACK("youtrack");
}
