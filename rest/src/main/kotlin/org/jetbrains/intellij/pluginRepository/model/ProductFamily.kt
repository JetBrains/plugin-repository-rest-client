package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonValue

enum class ProductFamily(@JsonValue val id: String) {
  TEAMCITY("teamcity"),
  TEAMCITY_RECIPES("teamcity_recipes"),
  INTELLIJ("intellij"),
  HUB("hub"),
  EDU("edu"),
  FLEET("fleet"),
  TOOLBOX("toolbox"),
  SPACE("space"),
  DOTNET("dotnet"),
  YOUTRACK("youtrack");
}
