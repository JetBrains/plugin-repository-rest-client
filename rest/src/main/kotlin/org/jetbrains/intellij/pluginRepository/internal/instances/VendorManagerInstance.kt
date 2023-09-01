package org.jetbrains.intellij.pluginRepository.internal.instances

import org.jetbrains.intellij.pluginRepository.VendorManager
import org.jetbrains.intellij.pluginRepository.internal.api.PluginRepositoryService
import org.jetbrains.intellij.pluginRepository.internal.utils.executeAndParseBody

class VendorManagerInstance(private val service: PluginRepositoryService): VendorManager {
  override fun getVendorById(vendorId: String) = executeAndParseBody(service.getVendorByName(vendorId))
}
