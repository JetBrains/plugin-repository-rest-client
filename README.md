# plugin-repository-rest-client [![JetBrains team project](https://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

Client and command line interface for [JetBrains Plugin Repository](https://plugins.jetbrains.com/).

The code snippet below will upload a new version of a plugin to the plugins repository.
```kotlin
val instance = PluginRepositoryInstance("https://plugins.jetbrains.com", "username", "password")
instance.uploadPlugin(pluginId, pluginZipFile)
```

```org.jetbrains.intellij.pluginRepository.Client``` main class provides command line interface for uploading, downloading and listing plugins on the plugin repository.

# Published on bintray
https://bintray.com/jetbrains/intellij-plugin-service/plugin-repository-rest-client
