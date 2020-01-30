package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.jetbrains.intellij.pluginRepository.model.ProductEnum
import org.junit.Assert
import org.junit.Test

class PluginManagerTest : BaseTest() {

  private val service = instance.pluginManager

  private val unknownPluginId = 5

  @Test
  fun `get plugin by id`() {
    val testPlugin = TestPlugins.LOMBOK_PLUGIN
    val plugin = service.getPlugin(testPlugin.id)
    Assert.assertNotNull(plugin)
    Assert.assertTrue(plugin?.xmlId == testPlugin.xmlId)
  }

  @Test
  fun `get plugin for unknown id`() {
    val plugin = service.getPlugin(unknownPluginId)
    Assert.assertNull(plugin)
  }

  @Test
  fun `get plugin developers`() {
    val testPlugin = TestPlugins.DOCKER
    val users = service.getPluginDevelopers(testPlugin.id)
    Assert.assertTrue(users.any { it.link == "/author/3eed2f7b-47e7-47b8-9d9b-86920094a87e" })
  }

  @Test
  fun `get plugin developers for unknown plugin`() {
    val users = service.getPluginDevelopers(unknownPluginId)
    Assert.assertTrue(users.isEmpty())
  }

  @Test
  fun `get plugin channels`() {
    val testPlugin = TestPlugins.EDUTOOLS
    val channels = service.getPluginChannels(testPlugin.id)
    Assert.assertTrue(channels.contains(""))
    Assert.assertTrue(channels.contains("beta"))
  }

  @Test
  fun `get plugin channels for unknown plugin`() {
    val channels = service.getPluginChannels(unknownPluginId)
    Assert.assertTrue(channels.isEmpty())
  }

  @Test
  fun `get plugin compatible products`() {
    val testPlugin = TestPlugins.KUBERNETES
    val products = service.getPluginCompatibleProducts(testPlugin.id)
    Assert.assertTrue(products.contains(ProductEnum.IDEA))
    Assert.assertTrue(products.contains(ProductEnum.CLION))
    Assert.assertTrue(products.contains(ProductEnum.RIDER))
    Assert.assertTrue(products.contains(ProductEnum.PYCHARM))
  }

  @Test
  fun `get plugin compatible products for unknown plugin`() {
    val products = service.getPluginCompatibleProducts(unknownPluginId)
    Assert.assertTrue(products.isEmpty())
  }

  @Test
  fun `search xml ids`() {
    val ids = service.getCompatiblePluginsXmlIds("IU-193.3", 100, 50)
    Assert.assertTrue(ids.size == 100)
    Assert.assertTrue(ids.contains("org.jetbrains.plugins.vagrant"))
  }

  @Test
  fun `search compatible updates ids`() {
    val updates = service.searchCompatibleUpdates(
      listOf("org.jetbrains.kotlin"), "IU-193.3")
    Assert.assertTrue(updates.isNotEmpty())
  }

  @Test
  fun `search compatible updates ids for wrong xml id`() {
    val updates = service.searchCompatibleUpdates(listOf("org.jetbrains.kotlin1"), "IU-193.3")
    Assert.assertTrue(updates.isEmpty())
  }
}