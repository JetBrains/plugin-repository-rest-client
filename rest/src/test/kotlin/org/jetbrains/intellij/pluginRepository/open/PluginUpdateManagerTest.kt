package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertTrue

class PluginUpdateManagerTest : BaseTest() {

  private val service = INSTANCE.pluginUpdateManager
  private val pluginService = INSTANCE.pluginManager

  private val unknownUpdateId = 39

  @Test
  fun `get update by id`() {
    val testPlugin = TestPlugins.GO
    val update = service.getUpdateById(testPlugin.updates.first())
    Assert.assertNotNull(update)
    Assert.assertTrue(update!!.version == "181.4668.90")
    val plugin = pluginService.getPlugin(update.pluginId)
    Assert.assertTrue(plugin?.name == testPlugin.pluginName)
  }

  @Test
  fun `get update for unknown id`() {
    val update = service.getUpdateById(unknownUpdateId)
    Assert.assertNull(update)
  }

  @Test
  fun `get by version & family`() {
    val testPlugin = TestPlugins.DATAGRIP
    val updates = service.getUpdatesByVersionAndFamily(testPlugin.xmlId, "193.5233.13")
    Assert.assertTrue(updates.size == 1)
    val update = updates.first()
    val plugin = pluginService.getPlugin(update.pluginId)
    Assert.assertTrue(plugin?.name == testPlugin.pluginName)
  }

  @Test
  fun `get by version & family for unknown id`() {
    val updates = service.getUpdatesByVersionAndFamily("testPlugin.xmlId", "193.5233.13")
    Assert.assertTrue(updates.isEmpty())
  }

  @Test
  fun `get intellij meta`() {
    val testPlugin = TestPlugins.KOTLIN
    val meta = service.getIntellijUpdateMetadata(testPlugin.id, testPlugin.updates.first())
    Assert.assertNotNull(meta)
    Assert.assertTrue(meta?.name == testPlugin.pluginName)
    Assert.assertTrue(meta?.vendor == "JetBrains")
    Assert.assertTrue(meta?.xmlId == testPlugin.xmlId)
  }

  @Test
  fun `get intellij meta for unknown id`() {
    val testPlugin = TestPlugins.KOTLIN
    val meta = service.getIntellijUpdateMetadata(testPlugin.id, unknownUpdateId)
    Assert.assertNull(meta)
  }


  @Test
  fun `get intellij meta batch`() {
    val testPlugin = TestPlugins.KOTLIN
    val testPlugin1 = TestPlugins.GO
    val metas = service.getIntellijUpdateMetadataBatch(listOf(
      Pair(testPlugin.id, testPlugin.updates.first()),
      Pair(testPlugin1.id, testPlugin1.updates.first())
    ))
    Assert.assertTrue(metas.isNotEmpty())
    Assert.assertTrue(metas.size == 2)

    val meta1 = metas[testPlugin.updates.first()]
    Assert.assertNotNull(meta1)
    Assert.assertTrue(meta1?.name == testPlugin.pluginName)
    Assert.assertTrue(meta1?.vendor == "JetBrains")
    Assert.assertTrue(meta1?.xmlId == testPlugin.xmlId)

    val meta2 = metas[testPlugin1.updates.first()]
    Assert.assertNotNull(meta2)
    Assert.assertTrue(meta2?.name == testPlugin1.pluginName)
    Assert.assertTrue(meta2?.vendor == "JetBrains")
    Assert.assertTrue(meta2?.xmlId == testPlugin1.xmlId)
  }

  @Test
  fun `get intellij meta batch for unknown 1`() {
    val testPlugin = TestPlugins.KOTLIN
    val testPlugin1 = TestPlugins.GO
    val metas = service.getIntellijUpdateMetadataBatch(listOf(
      Pair(testPlugin.id, testPlugin.updates.first()),
      Pair(testPlugin1.id, unknownUpdateId)
    ))
    Assert.assertTrue(metas.isNotEmpty())
    Assert.assertTrue(metas.size == 1)

    val meta1 = metas[testPlugin.updates.first()]
    Assert.assertNotNull(meta1)
    Assert.assertTrue(meta1?.name == testPlugin.pluginName)
    Assert.assertTrue(meta1?.vendor == "JetBrains")
    Assert.assertTrue(meta1?.xmlId == testPlugin.xmlId)

    val meta2 = metas[testPlugin1.updates.first()]
    Assert.assertNull(meta2)
  }

  @Test
  fun `get intellij meta batch for unknown ids`() {
    val testPlugin = TestPlugins.KOTLIN
    val testPlugin1 = TestPlugins.GO
    val metas = service.getIntellijUpdateMetadataBatch(listOf(
      Pair(testPlugin.id, unknownUpdateId),
      Pair(testPlugin1.id, unknownUpdateId)
    ))
    Assert.assertTrue(metas.isEmpty())

    val meta1 = metas[testPlugin.updates.first()]
    Assert.assertNull(meta1)

    val meta2 = metas[testPlugin1.updates.first()]
    Assert.assertNull(meta2)
  }

  @Test
  fun `get compatible plugin update test`() {
    getLastCompatiblePlugins("IU-193.5656")
  }

  @Test
  fun `get XML ids test`() {
    val ids = pluginService.getAllPluginsIds()
    assertTrue(ids.isNotEmpty(), "No IDS for plugins")
  }

  private fun getLastCompatiblePlugins(ideVersion: String) {
    val pluginsXmlIds = measureTimeMillisTest { pluginService.getCompatiblePluginsXmlIds(ideVersion, 10000, 0) }
    val updates = measureTimeMillisTest { pluginService.searchCompatibleUpdates(pluginsXmlIds, ideVersion) }
    println(updates.size)
    measureTimeMillisTest {
      service.getIntellijUpdateMetadataBatch(updates.map { it.pluginId to it.id })
    }
  }
}

inline fun <T> measureTimeMillisTest(block: () -> T): T {
  val start = System.currentTimeMillis()
  val b = block()
  println(System.currentTimeMillis() - start)
  return b
}
