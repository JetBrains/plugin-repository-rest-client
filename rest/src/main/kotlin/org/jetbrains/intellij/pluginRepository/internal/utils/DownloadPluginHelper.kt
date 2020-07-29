package org.jetbrains.intellij.pluginRepository.internal.utils

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
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.*

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
    throw IOException(Messages.getMessage("file.not.found", oldFile.toString()))
  }
  val oldBlockMap = FileInputStream(oldFile).buffered().use { input -> BlockMap(input) }

  val url = executed.raw().request.url.toUrl().toExternalForm()
  val fileName = url.removePrefix(url.replaceAfterLast("/", "")).removeSuffix(url.replaceBefore("?", ""))
  val baseUrl = url.replaceAfterLast("/", "")

  val retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(JacksonConverterFactory.create())
    .build()
  val service = retrofit.create(BlockMapService::class.java)

  val newBlockMap = executeExceptionally(service.getBlockMap()).body()
    ?: throw IOException(Messages.getMessage("blockmap.file.does.not.exist"))
  val newPluginHash = executeExceptionally(service.getHash()).body()
    ?: throw IOException(Messages.getMessage("hash.file.does.not.exist"))

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
