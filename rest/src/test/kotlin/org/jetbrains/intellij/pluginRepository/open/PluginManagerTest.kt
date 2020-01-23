package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.jetbrains.intellij.pluginRepository.model.ProductEnum
import org.junit.Assert
import org.junit.Test

class PluginManagerTest : BaseTest() {

  private val service = instance.pluginManager

  @Test
  fun `get plugin by id`() {
    val testPlugin = TestPlugins.LOMBOK_PLUGIN
    val plugin = service.getPlugin(testPlugin.id)
    Assert.assertNotNull(plugin)
    Assert.assertTrue(plugin?.xmlId == testPlugin.xmlId)
  }

  @Test
  fun `get plugin developers`() {
    val testPlugin = TestPlugins.DOCKER
    val users = service.getPluginDevelopers(testPlugin.id)
    Assert.assertTrue(users.any { it.link == "/author/3eed2f7b-47e7-47b8-9d9b-86920094a87e" })
  }

  @Test
  fun `get plugin channels`() {
    val testPlugin = TestPlugins.EDUTOOLS
    val channels = service.getPluginChannels(testPlugin.id)
    Assert.assertTrue(channels.contains(""))
    Assert.assertTrue(channels.contains("beta"))
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
  fun `search xml ids`() {
    val ids = service.getCompatiblePluginsXmlIds("IU-193.3", 100, 50)
    Assert.assertTrue(ids.size == 100)
    Assert.assertTrue(ids.contains("org.jetbrains.plugins.vagrant"))
  }

  @Test
  fun `search compatible updates ids`() {
    val updates = service.searchCompatibleUpdates(
      listOf("org.jetbrains.kotlin"),"IU-193.3")
    Assert.assertTrue(updates.isNotEmpty())
  }
}