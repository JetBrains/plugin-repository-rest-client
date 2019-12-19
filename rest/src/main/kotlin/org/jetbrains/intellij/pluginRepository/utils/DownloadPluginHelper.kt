package org.jetbrains.intellij.pluginRepository.utils

import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.LOG
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.nio.file.Files

internal fun downloadFile(executed: Response<ResponseBody>, targetPath: String): File? {
  val url = executed.raw().request().url().url().toExternalForm()
  val response = executed.body() ?: return null
  val mimeType = response.contentType()?.toString()
  if (mimeType != "application/zip" && mimeType != "application/java-archive") return null
  var targetFile = File(targetPath)
  if (targetFile.isDirectory) {
    val guessFileName = guessFileName(executed.raw(), url)
    if (guessFileName.contains(File.separatorChar)) {
      throw IOException(Messages.INVALID_FILENAME)
    }
    val file = File(targetFile, guessFileName)
    if (file.parentFile != targetFile) {
      throw IOException(Messages.INVALID_FILENAME)
    }
    targetFile = file
  }
  Files.copy(response.byteStream(), targetFile.toPath())
  LOG.info("Downloaded successfully to ${targetFile.absolutePath}")
  return targetFile
}

private fun guessFileName(response: okhttp3.Response, url: String): String {
  val filenameMarker = "filename="
  val contentDisposition = response.headers().names().find { it.equals("Content-Disposition", ignoreCase = true) }
  val contentDispositionHeader = contentDisposition?.let { response.headers().get(contentDisposition) }
  if (contentDispositionHeader == null || !contentDispositionHeader.contains(filenameMarker)) {
    val fileName = url.substringAfterLast('/')
    return if (fileName.isNotEmpty()) fileName else url
  }
  return contentDispositionHeader
    .substringAfter(filenameMarker, "")
    .substringBefore(';')
    .removeSurrounding("\"")
}
