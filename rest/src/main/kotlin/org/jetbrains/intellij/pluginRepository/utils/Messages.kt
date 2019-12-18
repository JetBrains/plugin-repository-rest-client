package org.jetbrains.intellij.pluginRepository.utils

internal object Messages {
  fun notFoundMessage(plugin: String?) = """
        Cannot find ${plugin ?: "plugin"}
        Note that you need to upload the plugin to the repository at least once manually 
        (to specify options like the license, repository URL etc.) before uploads through the client can be used. 
        """
  const val FAILED_UPLOAD = "Failed to upload plugin"
  const val MISSING_CONTENT_DISPOSITION = "Content-Disposition header should be set"
  const val MISSION_TOKEN = "Token must be set for uploading"
  const val MISSING_PLUGINS_PARAMETERS = "pluginId or XML id of plugin should be specified"
  const val INVALID_FILENAME = "Invalid filename returned by a server"

}