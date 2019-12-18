package org.jetbrains.intellij.pluginRepository

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.model.json.PluginInfoBean
import org.jetbrains.intellij.pluginRepository.model.xml.XmlPluginRepositoryBean
import retrofit2.Call
import retrofit2.http.*

interface PluginRepositoryService {
  @Multipart
  @Headers("Accept: text/plain")
  @POST("/plugin/uploadPlugin")
  fun upload(
    @Part("pluginId") pluginId: Int,
    @Part("channel") channel: String?,
    @Part("notes") notes: String?,
    @Part file: MultipartBody.Part
  ): Call<ResponseBody>

  @Multipart
  @Headers("Accept: text/plain")
  @POST("/plugin/uploadPlugin")
  fun uploadByXmlId(
    @Part("xmlId") pluginXmlId: String,
    @Part("channel") channel: String?,
    @Part("notes") notes: String?,
    @Part file: MultipartBody.Part
  ): Call<ResponseBody>

  @Multipart
  @POST("/api/plugins/{family}/upload")
  fun uploadNewPlugin(
    @Part file: MultipartBody.Part,
    @Path("family") family: String,
    @Query("licenseUrl") licenseUrl: String,
    @Part("cid") category: Int
  ): Call<PluginInfoBean>


  @Streaming
  @GET("/plugin/download")
  fun download(
    @Query("pluginId") pluginId: String,
    @Query("version") version: String,
    @Query("channel") channel: String?
  ): Call<ResponseBody>

  @Streaming
  @GET("/pluginManager?action=download")
  fun downloadCompatiblePlugin(
    @Query("id") pluginId: String,
    @Query("build") ideBuild: String,
    @Query("channel") channel: String?
  ): Call<ResponseBody>

  @GET("/plugins/list/")
  fun listPlugins(
    @Query("build") ideBuild: String,
    @Query("channel") channel: String?,
    @Query("pluginId") pluginId: String?
  ): Call<XmlPluginRepositoryBean>

  @GET("/api/plugins/{family}/{pluginXmlId}")
  fun pluginInfo(@Path("family") family: String, @Path("pluginXmlId") pluginXmlId: String): Call<PluginInfoBean>
}