dependencies {
  implementation("com.github.spullara.cli-parser:cli-parser:1.1.2")
  implementation(project(":services:plugin-repository-rest-client:rest"))
  implementation("org.slf4j:log4j-over-slf4j:1.7.26")
  implementation(libs.logback.classic)
  implementation(libs.slf4j.old.jcl)
  implementation(libs.slf4j.old.api)
}

