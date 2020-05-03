package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.TestPlugins
import org.jetbrains.intellij.pluginRepository.model.ProductEnum
import org.junit.Assert
import org.junit.Test

class PluginManagerTest : BaseTest() {

  private val service = INSTANCE.pluginManager

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
  fun `get plugin by xml id`() {
    val testPlugin = TestPlugins.LOMBOK_PLUGIN
    val plugin = service.getPluginByXmlId(testPlugin.xmlId)
    Assert.assertNotNull(plugin)
    Assert.assertTrue(plugin?.id == testPlugin.id)
  }

  @Test
  fun `get plugin for unknown xml id`() {
    val plugin = service.getPluginByXmlId("unknownPluginId")
    Assert.assertNull(plugin)
  }


  @Test
  fun `get plugin versions`() {
    val testPlugin = TestPlugins.KUBERNETES
    val updates = service.getPluginVersions(testPlugin.id)
    Assert.assertTrue(updates.isNotEmpty())
    val versions = updates.map { it.version }
    Assert.assertTrue(versions.contains("193.6015.53"))
    Assert.assertTrue(versions.contains("193.5662.31"))

    val plugins = updates.map { it.pluginXmlId }.distinct()
    Assert.assertTrue(plugins.size == 1)
    Assert.assertTrue(plugins.first() == testPlugin.xmlId)
  }

  @Test
  fun `get plugin versions for unknown id`() {
    val versions = service.getPluginVersions(unknownPluginId)
    Assert.assertTrue(versions.isEmpty())
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

  @Test
  fun `get plugin by dependency`(){
    val ids = service.getPluginXmlIdByDependency("com.intellij.modules.java")
    Assert.assertTrue(ids.isNotEmpty())
    Assert.assertTrue(ids.contains("org.jetbrains.kotlin"))
  }

  @Test
  fun `get plugin by dependency for optional`(){
    val ids = service.getPluginXmlIdByDependency("(optional) org.jetbrains.java.decompiler")
    Assert.assertTrue(ids.isNotEmpty())
    Assert.assertTrue(ids.contains("org.jetbrains.kotlin"))
    Assert.assertTrue(ids.contains("org.intellij.scala"))
  }

  @Test
  fun `get plugin by dependency unknown`(){
    val ids = service.getPluginXmlIdByDependency("(optional) org.blabal.java.decompiler")
    Assert.assertTrue(ids.isEmpty())
  }
}
