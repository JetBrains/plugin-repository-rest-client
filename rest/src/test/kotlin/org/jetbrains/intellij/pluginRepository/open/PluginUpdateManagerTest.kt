package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.junit.Assert
import org.junit.Test

class PluginUpdateManagerTest : BaseTest() {

  private val service = instance.pluginUpdateManager
  private val pluginService = instance.pluginManager

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
  fun `get by version & family`() {
    val testPlugin = TestPlugins.DATAGRIP
    val updates = service.getUpdatesByVersionAndFamily(testPlugin.xmlId, "193.5233.13")
    Assert.assertTrue(updates.size == 1)
    val update = updates.first()
    val plugin = pluginService.getPlugin(update.pluginId)
    Assert.assertTrue(plugin?.name == testPlugin.pluginName)
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

}