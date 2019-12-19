package org.jetbrains.intellij.pluginRepository.exceptions

class UploadFailedException(message: String?, cause: Throwable?) : Exception(message, cause)

fun uploadException(message: String?, cause: Throwable? = null): Nothing = throw UploadFailedException(message, cause)