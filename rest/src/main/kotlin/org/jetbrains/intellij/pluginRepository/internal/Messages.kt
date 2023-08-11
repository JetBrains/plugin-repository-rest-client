package org.jetbrains.intellij.pluginRepository.internal

import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.*

internal object Messages {

  private val messages by lazy {
    val properties = Properties()
    properties.load(this::class.java.getResourceAsStream("/errorMessages.properties"))
    properties
  }

  fun getMessage(@PropertyKey(resourceBundle = "errorMessages") key: String, vararg args: Any): String =
      MessageFormat.format(messages.getProperty(key), *args)

}
