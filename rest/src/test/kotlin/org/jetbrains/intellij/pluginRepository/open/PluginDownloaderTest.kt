package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.junit.Assert
import org.junit.Test
import java.io.File

class PluginDownloaderTest : BaseTest() {
  private val downloader = instance.downloader

  @Test
  fun `download plugin`() {
    val plugin = TestPlugins.GO
    val file = downloader.download(plugin.xmlId, "193.5233.12.46", "", File("."))
    validate(file)
  }

  @Test
  fun `download compatible plugin`() {
    val plugin = TestPlugins.KOTLIN
    val file = downloader.downloadLatestCompatiblePlugin(plugin.xmlId, "IC-145.184", "", File("."))
    validate(file)
  }

  private fun validate(file: File?) {
    Assert.assertNotNull(file)
    Assert.assertTrue(file!!.name.contains(".zip"))
    Assert.assertTrue(file.isFile)
    Assert.assertTrue(file.exists())
    Assert.assertTrue(file.length() > 0)
    file.deleteRecursively()
  }
}