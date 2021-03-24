package org.jetbrains.intellij.pluginRepository.open

import org.jetbrains.intellij.pluginRepository.PluginRepositoryFactory
import org.jetbrains.intellij.pluginRepository.base.BaseTest
import org.jetbrains.intellij.pluginRepository.base.REPOSITORY_HOST
import org.jetbrains.intellij.pluginRepository.internal.instances.PluginUploaderInstance
import org.junit.Test
import java.io.RandomAccessFile
import java.lang.IllegalArgumentException
import kotlin.io.path.ExperimentalPathApi

class PluginUploaderTest : BaseTest() {
  private val uploader = PluginRepositoryFactory.create(REPOSITORY_HOST, "test-token").uploader

  @ExperimentalPathApi
  @Test(expected = IllegalArgumentException::class)
  fun `upload huge new plugin test`() {
    val f = kotlin.io.path.createTempFile("file.jar").toFile()
    val rf = RandomAccessFile(f, "rw")
    rf.setLength(PluginUploaderInstance.MAX_FILE_SIZE + 10)
    uploader.uploadNewPlugin(f, 12, "url")
  }

  @ExperimentalPathApi
  @Test(expected = IllegalArgumentException::class)
  fun `upload huge plugin test`() {
    val f = kotlin.io.path.createTempFile("file.jar").toFile()
    val rf = RandomAccessFile(f, "rw")
    rf.setLength(PluginUploaderInstance.MAX_FILE_SIZE + 10)
    uploader.uploadPlugin(1347, f)
  }
}
