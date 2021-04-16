package org.jetbrains.intellij.pluginRepository.base

enum class TestPlugins(
  val pluginName : String,
  val xmlId : String,
  val id : Int,
  val updates : List<Int>
) {
  DATAGRIP("Database Tools and SQL", "com.intellij.database", 10925, listOf(75069)),
  DOCKER("Docker", "Docker", 7724, listOf(73789)),
  EDUTOOLS("EduTools", "com.jetbrains.edu", 10081, listOf()),
  RUBY("Ruby", "org.jetbrains.plugins.ruby", 1293, listOf()),
  GO("Go", "org.jetbrains.plugins.go", 9568, listOf(45394)),
  KOTLIN("Kotlin", "org.jetbrains.kotlin", 6954, listOf(73172)),
  KUBERNETES("Kubernetes", "com.intellij.kubernetes", 10485, listOf()),
  LOMBOK_PLUGIN("Lombok Plugin", "Lombook Plugin", 6317, listOf())
}
