import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("maven-publish")
  id("signing")
  alias(builds.plugins.publish.shadow)
}

val buildNumber = System.getenv("BUILD_NUMBER") ?: "SNAPSHOT"

allprojects {
  apply(plugin = "java")

  group = "org.jetbrains.intellij"
  version = "2.0.$buildNumber"

  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "kotlin")

  val javaVersion = "11"
  java {
    val jdkVersion = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = jdkVersion
    targetCompatibility = jdkVersion
    withSourcesJar()
    withJavadocJar()
  }

  kotlin {
    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget(javaVersion))
    }
  }
}

dependencies {
  implementation(project("cli"))
  implementation(project("rest"))
}

tasks {
  jar {
    manifest {
      attributes("Main-Class" to "org.jetbrains.intellij.pluginRepository.Client")
      attributes("Implementation-Version" to project.version)
      attributes("Implementation-Vendor" to "JetBrains s.r.o.")
      attributes("Implementation-Title" to "Plugin Repository Rest Client")
    }
  }
}

artifacts {
  archives(tasks.shadowJar)
}

publishing {
  publications {
    fun MavenPublication.configurePom() {
      pom {
        name.set("Plugin Repository Rest Client")
        description.set("The client and command line interface for JetBrains Marketplace.")
        url.set("https://github.com/JetBrains/plugin-repository-rest-client")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("AlexanderPrendota")
            name.set("Alexander Prendota")
            organization.set("JetBrains")
          }
          developer {
            id.set("zolotov")
            name.set("Alexander Zolotov")
            organization.set("JetBrains")
          }
          developer {
            id.set("serejke")
            name.set("Sergey Patrikeev")
            organization.set("JetBrains")
          }
          developer {
            id.set("chashnikov")
            name.set("Nikolay Chashnikov")
            organization.set("JetBrains")
          }
          developer {
            id.set("satamas")
            name.set("Semyon Atamas")
            organization.set("JetBrains")
          }
          developer {
            id.set("chrkv")
            name.set("Ivan Chirkov")
            organization.set("JetBrains")
          }
          developer {
            id.set("kesarevs")
            name.set("Sergei Kesarev")
            organization.set("JetBrains")
          }
          developer {
            id.set("yole")
            name.set("Dmitry Jemerov")
            organization.set("JetBrains")
          }
          developer {
            id.set("VladRassokhin")
            name.set("Vladislav Rassokhin")
            organization.set("JetBrains")
          }
          developer {
            id.set("hsz")
            name.set("Jakub Chrzanowski")
            organization.set("JetBrains")
          }
          developer {
            id.set("LChernigovskaya")
            name.set("Lidiya Chernigovskaya")
            organization.set("JetBrains")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/JetBrains/plugin-repository-rest-client.git")
          developerConnection.set("scm:git:ssh://github.com/JetBrains/plugin-repository-rest-client.git")
          url.set("https://github.com/JetBrains/plugin-repository-rest-client")
        }
      }
    }

    create<MavenPublication>("plugin-repository-rest-client") {
      groupId = project.group.toString()
      artifactId = "plugin-repository-rest-client"
      version = project.version.toString()

      from(project(":services:plugin-repository-rest-client:rest").components["java"])

      artifact(tasks.shadowJar) {
        classifier = "all"
      }
      configurePom()
    }
  }
}

signing {
  isRequired = buildNumber != "SNAPSHOT"

  val signingKey: String? by project
  val signingPassword: String? by project

  if(isRequired) {
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["plugin-repository-rest-client"])
  }
}
