package org.jetbrains.intellij.pluginRepository.internal.blockmap

import com.jetbrains.plugin.blockmap.core.BlockMap
import org.jetbrains.intellij.pluginRepository.internal.Messages
import org.jetbrains.intellij.pluginRepository.internal.api.BlockMapService
import org.jetbrains.intellij.pluginRepository.internal.utils.executeExceptionally
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

// According to Amazon CloudFront documentation the maximum length of a request,
// including the path, the query string (if any), and headers, is 20,480 bytes.
private const val MAX_HTTP_HEADERS_LENGTH: Int = 19500
private const val MAX_STRING_LENGTH: Int = 1024

class PluginChunkDataSource(
  oldBlockMap: BlockMap,
  newBlockMap: BlockMap,
  private val pluginFileService: BlockMapService,
  private val fileName: String
) : Iterator<ByteArray> {
  private val oldSet = oldBlockMap.chunks.toSet()
  private val chunksIterator = newBlockMap.chunks.filter { chunk -> !oldSet.contains(chunk) }.iterator()
  private var curRangeChunkLengths = ArrayList<Int>()
  private var curChunkData = getRange(nextRange())
  private var pointer: Int = 0

  override fun hasNext() = curChunkData.size != 0

  override fun next(): ByteArray {
    return if (curChunkData.size != 0) {
      if (pointer < curChunkData.size) {
        curChunkData[pointer++]
      } else {
        curChunkData = getRange(nextRange())
        pointer = 0
        next()
      }
    } else throw NoSuchElementException()
  }

  private fun nextRange(): String {
    val range = StringBuilder("bytes=")
    curRangeChunkLengths.clear()
    var rangeHeaderLength = 0
    while (chunksIterator.hasNext() && range.length <= MAX_HTTP_HEADERS_LENGTH) {
      val newChunk = chunksIterator.next()
      range.append("${newChunk.offset}-${newChunk.offset + newChunk.length - 1},")
      curRangeChunkLengths.add(newChunk.length)
      rangeHeaderLength += newChunk.length
    }
    return range.removeSuffix(",").toString()
  }

  private fun getRange(range: String): MutableList<ByteArray> {
    val result = ArrayList<ByteArray>()
    val executed = executeExceptionally(pluginFileService.getPluginFile(fileName, range))
    val contentType = executed.headers()["Content-Type"]
      ?: throw IOException(Messages.getMessage("http.response.content.type.null"))
    val boundary = contentType.removePrefix("multipart/byteranges; boundary=")
    val response = executed.body() ?: throw IOException(Messages.getMessage("http.response.body.null"))
    response.byteStream().buffered().use { input ->
      for (length in curRangeChunkLengths) {
        val openingEmptyLine = nextLine(input)
        if (openingEmptyLine != System.lineSeparator()) {
          throw IOException(Messages.getMessage("http.multirange.response.doesnt.include.line.separator"))
        }
        val boundaryLine = nextLine(input)
        if (!boundaryLine.contains(boundary)) {
          throw IOException(Messages.getMessage("http.multirange.response.doesnt.contain.boundary", boundaryLine, boundary))
        }
        val contentTypeLine = nextLine(input)
        if (!contentTypeLine.startsWith("Content-Type")) {
          throw IOException(Messages.getMessage("http.multirange.response.includes.incorrect.header", contentTypeLine, "Content-Type"))
        }
        val contentRangeLine = nextLine(input)
        if (!contentRangeLine.startsWith("Content-Range")) {
          throw IOException(Messages.getMessage("http.multirange.response.includes.incorrect.header", contentRangeLine, "Content-Range"))
        }
        val closingEmptyLine = nextLine(input)
        if (closingEmptyLine != System.lineSeparator()) {
          throw IOException(Messages.getMessage("http.multirange.response.doesnt.include.line.separator"))
        }
        val data = ByteArray(length)
        for (i in 0 until length) data[i] = input.read().toByte()
        result.add(data)
      }
    }
    return result
  }

  private fun nextLine(input: BufferedInputStream): String {
    ByteArrayOutputStream().use { baos ->
      do {
        val byte = input.read()
        baos.write(byte)
        if (baos.size() >= MAX_STRING_LENGTH) {
          throw IOException(Messages.getMessage("wrong.http.range.response", String(baos.toByteArray(), Charset.defaultCharset())))
        }
      } while (byte.toChar() != '\n')
      return String(baos.toByteArray(), Charset.defaultCharset())
    }
  }
}
