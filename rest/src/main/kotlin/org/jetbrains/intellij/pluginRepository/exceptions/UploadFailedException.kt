package org.jetbrains.intellij.pluginRepository.exceptions

class PluginRepositoryException(message: String?, cause: Throwable?) : Exception(message, cause)

class UploadFailedException(message: String?, cause: Throwable?) : Exception(message, cause)