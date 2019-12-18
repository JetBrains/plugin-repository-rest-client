package org.jetbrains.intellij.pluginRepository.exceptions

class PluginPluginRepositoryExceptionRepositoryRestException(message: String?, cause: Throwable?) : Exception(message, cause)

fun restException(message: String?, cause: Throwable? = null): Nothing = throw PluginPluginRepositoryExceptionRepositoryRestException(message, cause)