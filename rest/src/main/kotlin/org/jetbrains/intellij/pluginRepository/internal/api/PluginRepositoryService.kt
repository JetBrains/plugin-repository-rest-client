package org.jetbrains.intellij.pluginRepository.internal.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jetbrains.intellij.pluginRepository.model.json.CompatibleUpdateBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginUpdateBean
import org.jetbrains.intellij.pluginRepository.model.json.PluginUserBean
import org.jetbrains.intellij.pluginRepository.model.repository.IntellijUpdateMetadata
import org.jetbrains.intellij.pluginRepository.model.repository.ProductEnum
import org.jetbrains.intellij.pluginRepository.model.xml.XmlPluginRepositoryBean
import retrofit2.Call
import retrofit2.http.*

interface PluginRepositoryService {
  @Multipart
  @Headers("Accept: text/plain")
  @POST("/plugin/uploadPlugin")
  fun upload(
    @Part("pluginId") pluginId: Int,
    @Part("channel") channel: RequestBody?,
    @Part("notes") notes: RequestBody?,
    @Part file: MultipartBody.Part
  ): Call<ResponseBody>

  @Multipart
  @Headers("Accept: text/plain")
  @POST("/plugin/uploadPlugin")
  fun uploadByXmlId(
    @Part("xmlId") pluginXmlId: RequestBody,
    @Part("channel") channel: RequestBody?,
    @Part("notes") notes: RequestBody?,
    @Part file: MultipartBody.Part
  ): Call<ResponseBody>

  @Multipart
  @POST("/api/plugins/{family}/upload")
  fun uploadNewPlugin(
    @Part file: MultipartBody.Part,
    @Path("family") family: String,
    @Part("licenseUrl") licenseUrl: RequestBody,
    @Part("cid") category: Int
  ): Call<PluginBean>

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
  fun getPluginByXmlId(@Path("family") family: String, @Path("pluginXmlId") pluginXmlId: String): Call<PluginBean>

  @GET("/api/plugins/{id}")
  fun getPluginById(@Path("id") id: Int): Call<PluginBean>

  @GET("/api/plugins/{id}/developers")
  fun getPluginDevelopers(@Path("id") id: Int): Call<List<PluginUserBean>>

  @GET("/api/plugins/{id}/channels")
  fun getPluginChannels(@Path("id") id: Int): Call<List<String>>

  @GET("/api/plugins/{id}/compatibleProducts")
  fun getPluginCompatibleProducts(@Path("id") id: Int): Call<List<ProductEnum>>

  @GET("/api/plugins")
  fun getPluginXmlIdByDependency(
    @Query("dependency") dependency: String,
    @Query("includeOptional") includeOptional: Boolean
  ): Call<List<String>>

  @GET("/api/search")
  fun searchPluginsXmlIds(
    @Query("build") build: String,
    @Query("max") max: Int,
    @Query("offset") offset: Int,
    @Query("search") query: String
  ): Call<List<String>>


  @GET("/api/getCompatibleUpdates")
  fun getLastCompatibleUpdate(
    @Query("pluginXmlId") xmlId: String,
    @Query("build") build: String,
    @Query("channel") channel: String,
    @Query("max") max: Int
  ): Call<List<CompatibleUpdateBean>>

  @GET("/api/plugins/{id}/updates")
  fun getUpdatesByVersionAndFamily(
    @Path("id") xmlId: String,
    @Query("version") version: String,
    @Query("family") family: String
  ): Call<List<PluginUpdateBean>>

  @GET("/api/updates/{id}")
  fun getUpdateById(@Path("id") id: Int): Call<PluginUpdateBean>

  @GET("/files/{pluginId}/{updateId}/meta.json")
  fun getIntelliJUpdateMeta(@Path("pluginId") pluginId: Int, @Path("updateId") updateId: Int): Call<IntellijUpdateMetadata>

}