package org.jetbrains.intellij.pluginRepository

import org.slf4j.LoggerFactory
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Request
import retrofit.client.Response
import retrofit.client.UrlConnectionClient
import retrofit.http.*
import retrofit.mime.TypedFile
import retrofit.mime.TypedString
import java.io.File
import java.net.HttpURLConnection

/**
 * @author nik
 */
class PluginRepositoryInstance(val siteUrl: String, private val username: String?, private val password: String?) {
    private val service = RestAdapter.Builder()
            .setEndpoint(siteUrl)
            .setClient({ ->
                object : UrlConnectionClient() {
                    override fun openConnection(request: Request?): HttpURLConnection {
                        val connection = super.openConnection(request)
                        connection.instanceFollowRedirects = false
                        val timeout = 10 * 60 * 1000
                        connection.readTimeout = timeout
                        return connection
                    }
                }
            })
            .build()
            .create(PluginRepositoryService::class.java)

    fun uploadPlugin(pluginId: Int, file: File, channel: String? = null) {
        if (username == null) throw RuntimeException("Username must be set for uploading")
        if (password == null) throw RuntimeException("Password must be set for uploading")

        try {
            LOG.info("Uploading plugin $pluginId from ${file.absolutePath} to $siteUrl")
            service.upload(TypedString(username), TypedString(password), TypedString(pluginId.toString()),
                    channel?.let { TypedString(it) },
                    TypedFile("application/octet-stream", file))
        } catch(e: RetrofitError) {
            if (e.response?.status == 302) {
                LOG.info("Uploaded successfully")
                return
            }
            throw e;
        }
    }

    fun download(pluginPackageId: String, version: String, channel: String? = null, targetPath: String): File? {
        LOG.info("Downloading $pluginPackageId:$version")
        try {
            service.download(pluginPackageId, version, channel)
        } catch(e: RetrofitError) {
            if (e.response?.status == 302) {
                val file = downloadFile(e.response, targetPath)
                if (file != null) {
                    return file
                }
            }
        }
        LOG.error("Cannot find $pluginPackageId:$version")
        return null

    }

    fun downloadCompatiblePlugin(pluginPackageId: String, ideBuild: String, channel: String? = null,
                                 targetPath: String): File? {
        LOG.info("Downloading $pluginPackageId for $ideBuild build")
        try {
            service.downloadCompatiblePlugin(pluginPackageId, ideBuild, channel)
        } catch(e: RetrofitError) {
            if (e.response?.status == 302) {
                val file = downloadFile(e.response, targetPath)
                if (file != null) {
                    return file
                }
            }
        }
        LOG.error("Cannot find $pluginPackageId compatible with $ideBuild build")
        return null
    }

    private fun downloadFile(response: Response, targetPath: String): File? {
        for (header in response.headers) {
            if (header.name.equals("location", true)) {
                val fileLocation = header.value
                if (!fileLocation.isNullOrBlank()) {
                    val downloadResponse = UrlConnectionClient().execute(Request("GET", fileLocation, listOf(), null))
                    if (downloadResponse.status == 200) {
                        val mimeType = downloadResponse.body.mimeType()
                        if (mimeType == "application/zip" || mimeType == "application/java-archive") {
                            var targetFile = File(targetPath)
                            if (targetFile.isDirectory) {
                                val index = fileLocation.lastIndexOf('/')
                                val fileName = if (index > 0) fileLocation.substring(index) else fileLocation
                                targetFile = File(targetFile, fileName)
                            }
                            if (!targetFile.createNewFile()) {
                                throw RuntimeException("Cannot create ${targetFile.absolutePath}")
                            }

                            downloadResponse.body.`in`().copyTo(targetFile.outputStream())
                            LOG.info("Downloaded successfully to ${targetFile.absolutePath}")
                            return targetFile
                        }
                    }
                }
            }
        }
        return null
    }
}

private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")

private interface PluginRepositoryService {
    @Multipart
    @POST("/plugin/uploadPlugin")
    fun upload(@Part("userName") username: TypedString, @Part("password") password: TypedString,
               @Part("pluginId") pluginId: TypedString, @Part("channel") channel: TypedString?,
               @Part("file") file: TypedFile): Response

    @GET("/plugin/download")
    fun download(@Query("pluginId") pluginId: String, @Query("version") version: String,
                 @Query("channel") channel: String?): Response

    @GET("/pluginManager?action=download")
    fun downloadCompatiblePlugin(@Query("id") pluginId: String, @Query("build") ideBuild: String,
                                 @Query("channel") channel: String?): Response
}