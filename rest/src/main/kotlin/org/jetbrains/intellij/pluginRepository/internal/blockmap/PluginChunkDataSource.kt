package org.jetbrains.intellij.pluginRepository.internal.blockmap

import com.jetbrains.plugin.blockmap.core.BlockMap
import com.jetbrains.plugin.blockmap.core.Chunk
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
private const val MAX_RANGE_BYTES: Int = 10_000_000
private const val MAX_STRING_LENGTH: Int = 1024

class PluginChunkDataSource(
  oldBlockMap: BlockMap,
  newBlockMap: BlockMap,
  private val pluginFileService: BlockMapService,
  private val fileName: String
) : Iterator<ByteArray> {
  private val oldSet = oldBlockMap.chunks.toSet()
  private val chunks = newBlockMap.chunks.filter { chunk -> !oldSet.contains(chunk) }
  private var pos = 0
  private var chunkSequences = ArrayList<ArrayList<Chunk>>()
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
    chunkSequences.clear()
    var bytes = 0
    while (pos < chunks.size && range.length <= MAX_HTTP_HEADERS_LENGTH) {
      val chunkSequence = nextChunkSequence(bytes)
      chunkSequences.add(chunkSequence)
      bytes += chunkSequence.last().offset + chunkSequence.last().length - chunkSequence[0].offset
      range.append("${chunkSequence[0].offset}-${chunkSequence.last().offset + chunkSequence.last().length - 1},")
    }
    return range.removeSuffix(",").toString()
  }

  private fun nextChunkSequence(bytes: Int): ArrayList<Chunk> {
    val result = ArrayList<Chunk>()
    result.add(chunks[pos])
    pos++
    var sum = result[0].length
    while (pos < chunks.size - 1
      && chunks[pos].offset == chunks[pos - 1].offset + chunks[pos - 1].length
      && sum + bytes < MAX_RANGE_BYTES) {
      result.add(chunks[pos])
      sum += chunks[pos].length
      pos++
    }
    return result
  }

  private fun getRange(range: String): MutableList<ByteArray> {
    val result = ArrayList<ByteArray>()
    val executed = executeExceptionally(pluginFileService.getPluginFile(fileName, range))
    val contentType = executed.headers()["Content-Type"]
      ?: throw IOException(Messages.getMessage("http.response.content.type.null"))
    val boundary = contentType.removePrefix("multipart/byteranges; boundary=")
    val response = executed.body() ?: throw IOException(Messages.getMessage("http.response.body.null"))
    response.byteStream().buffered().use { input ->
      for (sequence in chunkSequences) {
        val openingEmptyLine = nextLine(input)
        if (openingEmptyLine.trim().isNotEmpty()) {
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
        if (closingEmptyLine.trim().isNotEmpty()) {
          throw IOException(Messages.getMessage("http.multirange.response.doesnt.include.line.separator"))
        }
        for (chunk in sequence) {
          val data = ByteArray(chunk.length)
          for (i in 0 until chunk.length) data[i] = input.read().toByte()
          result.add(data)
        }
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
