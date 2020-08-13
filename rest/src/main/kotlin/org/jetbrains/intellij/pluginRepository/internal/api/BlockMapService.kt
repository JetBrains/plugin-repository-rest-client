package org.jetbrains.intellij.pluginRepository.internal.api

import com.jetbrains.plugin.blockmap.core.FileHash
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface BlockMapService {
  @GET("{fileName}")
  fun getPluginFile(
    @Path("fileName") fileName: String,
    @Header("Range") range: String
  ): Call<ResponseBody>

  @GET("{fileName}")
  fun getBlockMapZip(@Path("fileName") fileName : String): Call<ResponseBody>

  @GET("{fileName}")
  fun getHash(@Path("fileName") fileName : String): Call<FileHash>
}
