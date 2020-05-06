package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.junit.Assert
import org.junit.Test
import java.io.File

class PluginDownloaderTest : BaseTest() {
  private val downloader = NON_AUTH_INSTANCE.downloader
  private val downloadPath = System.getProperty("jetbrains.plugin.download.path")

  @Test
  fun `download plugin update by version and xml ID`() {
    val plugin = TestPlugins.GO
    val file = downloader.download(plugin.xmlId, "172.3757.46", File(downloadPath), "")
    validate(file)
  }

  @Test
  fun `download by update id`() {
    val plugin = TestPlugins.DOCKER
    val file = downloader.download(plugin.updates.first(), File(downloadPath))
    validate(file)
  }

  @Test
  fun `download incompatible plugin vaadin for some IDE`() {
    val file = downloader.downloadLatestCompatiblePlugin("intellij.indexing.shared", "IU-193.6494.35", File(downloadPath), "")
    Assert.assertNull(file)
  }

  private fun validate(file: File?) {
    Assert.assertNotNull(file)
    Assert.assertTrue("File w/o extension: ${file?.name}.", file!!.name.contains(".zip"))
    Assert.assertTrue("isFile is not true", file.isFile)
    Assert.assertTrue("File does not exist", file.exists())
    Assert.assertTrue("File length <= 0", file.length() > 0)
  }
}
