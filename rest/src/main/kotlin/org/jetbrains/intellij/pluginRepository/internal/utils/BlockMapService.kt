package org.jetbrains.intellij.pluginRepository.internal.utils

import com.jetbrains.plugin.blockmap.core.FileHash
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

internal const val BLOCKMAP_ZIP = "blockmap.zip"

internal const val BLOCKMAP_FILENAME = "blockmap.json"

internal const val HASH_FILENAME = "hash.json"

interface BlockMapService {
  @GET("{fileName}")
  fun getPluginFile(
    @Path("fileName") fileName: String,
    @Header("Range") range: String
  ): Call<ResponseBody>

  @GET(BLOCKMAP_ZIP)
  fun getBlockMapZip(): Call<ResponseBody>

  @GET(HASH_FILENAME)
  fun getHash(): Call<FileHash>
}
