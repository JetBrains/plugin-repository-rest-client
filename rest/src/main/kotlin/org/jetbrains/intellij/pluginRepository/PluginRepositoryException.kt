package org.jetbrains.intellij.pluginRepository

class PluginRepositoryException : Exception {

  constructor(message: String) : super(message)

  constructor(cause: Throwable) : super(cause)

  constructor(message: String, cause: Throwable) : super(message, cause)

}