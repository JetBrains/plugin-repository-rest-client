package org.jetbrains.intellij.pluginRepository.exceptions

class PluginRepositoryException(message: String?, cause: Throwable?) : Exception(message, cause)

fun restException(message: String?, cause: Throwable? = null): Nothing = throw PluginRepositoryException(message, cause)