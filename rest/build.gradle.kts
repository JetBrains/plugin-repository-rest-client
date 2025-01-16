dependencies {
  implementation("com.squareup.retrofit2:retrofit:2.11.0") {
    exclude(module = "okhttp")
  }
  implementation("com.squareup.retrofit2:converter-jaxb:2.11.0")
  implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
  implementation(libs.jackson.databind)
  implementation(libs.okhttp)
  implementation(libs.jackson.kotlin)
  implementation("com.sun.xml.bind:jaxb-impl:2.3.2")
  implementation("com.sun.xml.bind:jaxb-core:2.3.0")
  implementation("org.jetbrains.intellij:blockmap:1.0.5")
  testImplementation(libs.junit)
  testImplementation("org.assertj:assertj-core:3.12.2")
  implementation(libs.slf4j.old.api)
}
