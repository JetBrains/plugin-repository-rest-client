# Plugin Repository Rest Client [![JetBrains team project](https://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

The client and command line interface for [JetBrains Plugin Repository](https://plugins.jetbrains.com/).

# Examples
The code snippet below will initiate the instance of the JetBrains Marketplace repository.

```kotlin
val instance = PluginRepositoryFactory.create("https://plugins.jetbrains.com", "authToken")
```

### Download plugin

```kotlin
instance.downloader.download("org.jetbrains.plugins.go", version, into, channel)
```

### Upload update & new plugins

The code snippet below will init the instance of the JetBrains Marketplace repository. You need to provide a [permanent hub token](https://www.jetbrains.com/help/youtrack/standalone/Manage-Permanent-Token.html) to authorize.

```kotlin
// upload update to existing plugin.
instance.uploader.uploadPlugin("org.jetbrains.kotlin", file, channel, notes)
// upload new plugin into Marketplace repository.
instance.uploader.uploadNewPlugin(file, categoryId, licenseUrl)
```

### Plugin info

```kotlin

val pluginMeta = instance.pluginUpdateManager.getIntellijUpdateMetadata(pluginId, updateId)

val updateInfo = instance.pluginUpdateManager.getUpdatesByVersionAndFamily("org.jetbrains.kotlin", version, family)

val plugin = instance.pluginManager.getPlugin(pluginId)

```

### Client

The `org.jetbrains.intellij.pluginRepository.Client` main class provides command line interface for uploading, downloading and listing plugins on the plugin repository.

# Published on bintray
https://bintray.com/jetbrains/intellij-plugin-service/plugin-repository-rest-client
