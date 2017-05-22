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
            .setLog({ LOG.debug(it) })
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .build()
            .create(PluginRepositoryService::class.java)

    fun uploadPlugin(pluginId: Int, file: File, channel: String? = null) {
        ensureCredentialsAreSet()
        try {
            LOG.info("Uploading plugin $pluginId from ${file.absolutePath} to $siteUrl")
            service.upload(TypedString(username), TypedString(password), TypedString(pluginId.toString()),
                    channel?.let { TypedString(it) }, TypedFile("application/octet-stream", file))
        } catch(e: RetrofitError) {
            handleUploadResponse(e)
            throw e
        }
    }

    fun uploadPlugin(pluginXmlId: String, file: File, channel: String? = null) {
        ensureCredentialsAreSet()
        try {
            LOG.info("Uploading plugin $pluginXmlId from ${file.absolutePath} to $siteUrl")
            service.uploadByXmlId(TypedString(username), TypedString(password), TypedString(pluginXmlId),
                    channel?.let { TypedString(it) }, TypedFile("application/octet-stream", file))
        } catch(e: RetrofitError) {
            handleUploadResponse(e)
            throw e
        }
    }

    private fun handleUploadResponse(e: RetrofitError) {
        if (e.response?.status == 302) {
            LOG.info("Uploaded successfully")
            return
        }
        throw e
    }

    private fun ensureCredentialsAreSet() {
        if (username == null) throw RuntimeException("Username must be set for uploading")
        if (password == null) throw RuntimeException("Password must be set for uploading")
    }

    fun download(pluginXmlId: String, version: String, channel: String? = null, targetPath: String): File? {
        LOG.info("Downloading $pluginXmlId:$version")
        try {
            service.download(pluginXmlId, version, channel)
        } catch(e: RetrofitError) {
            if (e.response?.status == 302) {
                val file = downloadFile(e.response, targetPath)
                if (file != null) {
                    return file
                }
            }
        }
        LOG.error("Cannot find $pluginXmlId:$version")
        return null

    }

    fun downloadCompatiblePlugin(pluginXmlId: String, ideBuild: String, channel: String? = null,
                                 targetPath: String): File? {
        LOG.info("Downloading $pluginXmlId for $ideBuild build")
        try {
            service.downloadCompatiblePlugin(pluginXmlId, ideBuild, channel)
        } catch(e: RetrofitError) {
            if (e.response?.status == 302) {
                val file = downloadFile(e.response, targetPath)
                if (file != null) {
                    return file
                }
            }
        }
        LOG.error("Cannot find $pluginXmlId compatible with $ideBuild build")
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
                                targetFile = File(targetFile, guessFileName(downloadResponse, fileLocation))
                            }
                            if (!targetFile.createNewFile()) {
                                throw RuntimeException("Cannot create ${targetFile.absolutePath}")
                            }
                            targetFile.outputStream().use { downloadResponse.body.`in`().copyTo(it) }
                            LOG.info("Downloaded successfully to ${targetFile.absolutePath}")

                            return targetFile
                        }
                    }
                }
            }
        }
        return null
    }

    private fun guessFileName(response: Response, url: String): String {
        for (header in response.headers) {
            val filenameMarker = "filename="
            if (header.name.equals("Content-Disposition", true)) {
                if (header.value.contains(filenameMarker)) {
                    return header.value.substringAfter(filenameMarker, "").substringBefore(';').removeSurrounding("\"")
                }
                break
            }
        }
        val fileName = url.substringAfterLast('/')
        return if (fileName.isNotEmpty()) fileName else url
    }
}

private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")

private interface PluginRepositoryService {
    @Multipart
    @POST("/plugin/uploadPlugin")
    fun upload(@Part("userName") username: TypedString, @Part("password") password: TypedString,
               @Part("pluginId") pluginId: TypedString, @Part("channel") channel: TypedString?,
               @Part("file") file: TypedFile): Response

    @Multipart
    @POST("/plugin/uploadPlugin")
    fun uploadByXmlId(@Part("userName") username: TypedString, @Part("password") password: TypedString,
                      @Part("xmlId") pluginXmlId: TypedString, @Part("channel") channel: TypedString?,
                      @Part("file") file: TypedFile): Response


    @GET("/plugin/download")
    fun download(@Query("pluginId") pluginId: String, @Query("version") version: String,
                 @Query("channel") channel: String?): Response

    @GET("/pluginManager?action=download")
    fun downloadCompatiblePlugin(@Query("id") pluginId: String, @Query("build") ideBuild: String,
                                 @Query("channel") channel: String?): Response
}