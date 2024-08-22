package org.jetbrains.intellij.pluginRepository.internal.utils

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.PluginRepositoryException
import org.jetbrains.intellij.pluginRepository.internal.Messages
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.nio.file.Files
import com.jetbrains.plugin.blockmap.core.BlockMap
import com.jetbrains.plugin.blockmap.core.ChunkMerger
import com.jetbrains.plugin.blockmap.core.FileHash
import org.jetbrains.intellij.pluginRepository.internal.api.BlockMapService
import org.jetbrains.intellij.pluginRepository.internal.api.LOG
import org.jetbrains.intellij.pluginRepository.internal.blockmap.PluginChunkDataSource
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.*
import java.util.zip.ZipInputStream

private val objectMapper by lazy { ObjectMapper() }

internal const val BLOCKMAP_ZIP_SUFFIX = ".blockmap.zip"

internal const val BLOCKMAP_FILENAME = "blockmap.json"

internal const val HASH_FILENAME_SUFFIX = ".hash.json"

private const val MAXIMUM_DOWNLOAD_PERCENT = 0.65 // 100% = 1.0

internal fun downloadPlugin(callable: Call<ResponseBody>, targetPath: File): File? {
  val response = executeExceptionally(callable)
  if (response.isSuccessful) {
    return try {
      downloadFile(response, targetPath)
    } catch (e: Exception) {
      throw PluginRepositoryException(Messages.getMessage("downloading.failed", response.code()), e)
    }
  }
  if (response.code() == 404) return null
  throw PluginRepositoryException(Messages.getMessage("downloading.failed", response.code()) + getResponseErrorMessage(response))
}

internal fun downloadPluginViaBlockMap(callable: Call<ResponseBody>, targetPath: File, oldFile: File): File? {
  val response = executeExceptionally(callable)
  if (response.isSuccessful) {
    return try {
      downloadFileViaBlockMap(response, targetPath, oldFile)
    } catch (e: Exception) {
      throw PluginRepositoryException(Messages.getMessage("downloading.failed", response.code()), e)
    }
  }
  if (response.code() == 404) return null
  throw PluginRepositoryException(Messages.getMessage("downloading.failed", response.code()) + getResponseErrorMessage(response))
}

private fun downloadFile(executed: Response<ResponseBody>, targetPath: File): File? {
  val url = executed.raw().request.url.toUrl().toExternalForm()
  val response = executed.body() ?: return null
  val mimeType = response.contentType()?.toString()
  if (mimeType != "application/zip" && mimeType != "application/java-archive") return null
  val targetFile = getTargetFile(targetPath, executed, url)
  Files.copy(response.byteStream(), targetFile.toPath())
  return targetFile
}

private fun downloadFileViaBlockMap(executed: Response<ResponseBody>, targetPath: File, oldFile: File): File? {
  if (!oldFile.exists()) {
    LOG.info(Messages.getMessage("file.not.found", oldFile.toString()))
    return downloadFile(executed, targetPath)
  }

  val url = executed.raw().request.url.toUrl().toExternalForm()
  val fileName = url.removePrefix(url.replaceAfterLast("/", "")).removeSuffix(url.replaceBefore("?", ""))
  val baseUrl = url.replaceAfterLast("/", "")

  val retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(JacksonConverterFactory.create())
    .build()
  val service = retrofit.create(BlockMapService::class.java)

  val blockMapFileName = "$fileName$BLOCKMAP_ZIP_SUFFIX"
  val hashFileName = "$fileName$HASH_FILENAME_SUFFIX"

  try {
    val blockMapZip = executeExceptionally(service.getBlockMapZip(blockMapFileName)).body()
      ?: throw IOException(Messages.getMessage("block.map.file.doesnt.exist"))
    val newBlockMap = getBlockMapFromZip(blockMapZip.byteStream())
    val newPluginHash = executeExceptionally(service.getHash(hashFileName)).body()
      ?: throw IOException(Messages.getMessage("hash.file.does.not.exist"))

    val oldBlockMap = FileInputStream(oldFile).use { input ->
      BlockMap(input, newBlockMap.algorithm, newBlockMap.minSize, newBlockMap.maxSize, newBlockMap.normalSize)
    }

    val downloadPercent = downloadPercent(oldBlockMap, newBlockMap)
    LOG.info("Plugin's download percent is = %.2f".format(downloadPercent * 100))
    if (downloadPercent > MAXIMUM_DOWNLOAD_PERCENT) {
      throw IOException(Messages.getMessage("too.large.download.size"))
    }

    val merger = ChunkMerger(oldFile, oldBlockMap, newBlockMap)

    val targetFile = getTargetFile(targetPath, executed, url)
    FileOutputStream(targetFile).use { output ->
      merger.merge(output, PluginChunkDataSource(oldBlockMap, newBlockMap, service, fileName))
    }

    val curFileHash = FileInputStream(targetFile).use { input -> FileHash(input, newPluginHash.algorithm) }
    if (curFileHash != newPluginHash) {
      throw IOException(Messages.getMessage("hashes.doesnt.match"))
    }

    return targetFile
  } catch (e: Exception) {
    LOG.info("Unable to download plugin via blockmap: ${e.message}")
    return downloadPlugin(service.getPluginFile(fileName, ""), targetPath)
  }
}

private fun downloadPercent(oldBlockMap: BlockMap, newBlockMap: BlockMap): Double {
  val oldSet = oldBlockMap.chunks.toSet()
  val newChunks = newBlockMap.chunks.filter { chunk -> !oldSet.contains(chunk) }
  return newChunks.sumOf { chunk -> chunk.length }.toDouble() /
    newBlockMap.chunks.sumOf { chunk -> chunk.length }.toDouble()
}

private fun getBlockMapFromZip(input: InputStream): BlockMap {
  return input.buffered().use { source ->
    ZipInputStream(source).use { zip ->
      var entry = zip.nextEntry
      while (entry.name != BLOCKMAP_FILENAME && entry.name != null) entry = zip.nextEntry
      if (entry.name == BLOCKMAP_FILENAME) {
        // there is must only one entry otherwise we can't properly
        // read entry because we don't know it size (entry.size returns -1)
        objectMapper.readValue(zip.readBytes(), BlockMap::class.java)
      } else {
        throw IOException(Messages.getMessage("block.map.file.doesnt.exist"))
      }
    }
  }
}

private fun getTargetFile(targetPath: File, executed: Response<ResponseBody>, url: String): File {
  var targetFile = targetPath
  if (targetFile.isDirectory) {
    val guessFileName = guessFileName(executed.raw(), url)
    if (guessFileName.contains(File.separatorChar)) {
      throw IOException(Messages.getMessage("invalid.filename"))
    }
    val file = File(targetFile, guessFileName)
    if (file.parentFile != targetFile) {
      throw IOException(Messages.getMessage("invalid.filename"))
    }
    targetFile = file
  }
  return targetFile
}

private fun getResponseErrorMessage(response: Response<ResponseBody>): String {
  return (response.errorBody()?.string() ?: response.message() ?: "").let { if (it.isNotEmpty()) ": $it" else "" }
}

private fun guessFileName(response: okhttp3.Response, url: String): String {
  val filenameMarker = "filename="
  val contentDisposition = response.headers.names().find { it.equals("Content-Disposition", ignoreCase = true) }
  val contentDispositionHeader = contentDisposition?.let { response.headers[contentDisposition] }
  if (contentDispositionHeader == null || !contentDispositionHeader.contains(filenameMarker)) {
    val fileName = url.substringAfterLast('/')
    return if (fileName.isNotEmpty()) fileName else url
  }
  return contentDispositionHeader
    .substringAfter(filenameMarker, "")
    .substringBefore(';')
    .removeSurrounding("\"")
}
