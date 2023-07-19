package org.jetbrains.intellij.pluginRepository.model

import com.fasterxml.jackson.annotation.JsonValue

enum class ProductFamily(@JsonValue val id: String) {
  TEAMCITY("teamcity"),
  INTELLIJ("intellij"),
  HUB("hub"),
  EDU("edu"),
  FLEET("fleet"),
  SPACE("space"),
  DOTNET("dotnet");
}
