package org.jetbrains.intellij.pluginRepository.model.repository

import com.fasterxml.jackson.annotation.JsonValue

enum class ProductFamily(@JsonValue val id: String) {
  TEAMCITY("teamcity"),
  INTELLIJ("intellij"),
  HUB("hub"),
  DOTNET("dotnet");
}
