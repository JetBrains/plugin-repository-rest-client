package org.jetbrains.intellij.pluginRepository

import org.jetbrains.intellij.pluginRepository.exceptions.UploadFailedException
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.slf4j.LoggerFactory
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Request
import retrofit.client.Response
import retrofit.client.UrlConnectionClient
import retrofit.converter.SimpleXMLConverter
import retrofit.http.*
import retrofit.mime.TypedFile
import retrofit.mime.TypedString
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection

@Root(strict = false)
private data class RestPluginRepositoryBean(
        @field:ElementList(entry = "category", inline = true, required = false) var categories: List<RestCategoryBean>? = null
)

@Root(strict = false)
private data class RestCategoryBean(
        @field:Attribute var name: String? = null,
        @field:ElementList(entry = "idea-plugin", inline = true) var plugins: List<RestPluginBean>? = null
)

@Root(strict = false)
private data class RestPluginBean(
        @param:Element(name = "name") @field:Element val name: String,
        @param:Element(name = "id") @field:Element val id: String,
        @param:Element(name = "version") @field:Element val version: String,
        @param:Element(name = "idea-version") @field:Element(name = "idea-version") val ideaVersion: RestIdeaVersionBean,
        @param:ElementList(entry = "depends", inline = true, required = false) @field:ElementList(entry = "depends", inline = true, required = false) val depends: List<String>? = null
)

@Root(strict = false)
private data class RestIdeaVersionBean(
        @field:Attribute(name = "since-build", required = false) var sinceBuild: String? = null,
        @field:Attribute(name = "until-build", required = false) var untilBuild: String? = null
)

data class PluginBean(
        val name: String,
        val id: String,
        val version: String,
        val category: String,
        val sinceBuild: String?,
        val untilBuild: String?,
        val depends: List<String>
)

/**
 * @author nik
 */
class PluginRepositoryInstance private constructor(
        private val siteUrl: String,
        private val token: String?,
        username: String?,
        password: String?
) {
    @Deprecated("Use hub permanent tokens to authorize your requests")
    constructor(siteUrl: String, username: String?, password: String?) : this(siteUrl, null, username, password)

    /**
     * @param siteUrl url of plugins repository instance. For example: https://plugins.jetbrains.com
     * @param token hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
     */
    constructor(siteUrl: String, token: String? = null) : this(siteUrl, token, null, null)

    private val username = if (username != null) TypedString(username) else null
    private val password = if (password != null) TypedString(password) else null

    private val service = RestAdapter.Builder()
            .setEndpoint(siteUrl)
            .setClient({ ->
                object : UrlConnectionClient() {
                    override fun openConnection(request: Request?): HttpURLConnection {
                        val connection = super.openConnection(request)
                        val timeout = 10 * 60 * 1000
                        connection.readTimeout = timeout
                        return connection
                    }
                }
            })
            .setRequestInterceptor({ request ->
                if (token != null) request.addHeader("Authorization", "Bearer $token")
            })
            .setLog({ LOG.debug(it) })
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setConverter(SimpleXMLConverter())
            .build()
            .create(PluginRepositoryService::class.java)

    fun uploadPlugin(pluginId: Int, file: File, channel: String? = null) {
        uploadPluginInternal(file, pluginId = pluginId, channel = channel)
    }

    fun uploadPlugin(pluginXmlId: String, file: File, channel: String? = null) {
        uploadPluginInternal(file, pluginXmlId = pluginXmlId, channel = channel)
    }

    private fun uploadPluginInternal(file: File, pluginId: Int? = null, pluginXmlId: String? = null, channel: String? = null) {
        ensureCredentialsAreSet()
        try {
            LOG.info("Uploading plugin ${pluginXmlId ?: pluginId} from ${file.absolutePath} to $siteUrl")
            val response = if (pluginXmlId != null) {
                service.uploadByXmlId(username, password, TypedString(pluginXmlId),
                        channel?.let { TypedString(it) }, TypedFile("application/octet-stream", file))
            } else {
                service.upload(username, password, TypedString(pluginId.toString()),
                        channel?.let { TypedString(it) }, TypedFile("application/octet-stream", file))
            }
            LOG.info("Done: " + response.text)
        } catch (e: RetrofitError) {
            //see `retrofit.RetrofitError.Kind.UNEXPECTED` doc
            if (e.kind == RetrofitError.Kind.UNEXPECTED) throw e.cause!!
            val message = if (e.response != null) e.response.text else e.message
            LOG.error("Failed to upload plugin: $message")
            throw UploadFailedException(message)
        }
    }

    private fun ensureCredentialsAreSet() {
        if (token != null) return
        if (username == null) throw RuntimeException("Username must be set for uploading")
        if (password == null) throw RuntimeException("Password must be set for uploading")
    }

    fun download(pluginXmlId: String, version: String, channel: String? = null, targetPath: String): File? {
        LOG.info("Downloading $pluginXmlId:$version")
        return try {
            downloadFile(service.download(pluginXmlId, version, channel), targetPath)
        } catch (e: RetrofitError) {
            processRetofitError(e, "Cannot find $pluginXmlId:$version", "Can't download plugin")
            null
        }
    }

    fun downloadCompatiblePlugin(pluginXmlId: String, ideBuild: String, channel: String? = null,
                                 targetPath: String): File? {
        LOG.info("Downloading $pluginXmlId for $ideBuild build")
        return try {
            downloadFile(service.downloadCompatiblePlugin(pluginXmlId, ideBuild, channel), targetPath)
        } catch (e: RetrofitError) {
            processRetofitError(e, "Cannot find $pluginXmlId compatible with $ideBuild build", "Can't download plugin")
            null
        }
    }

    private fun processRetofitError(e: RetrofitError, notFoundErrorMessage: String, baseErrorMessage: String) {
        //see `retrofit.RetrofitError.Kind.UNEXPECTED` doc
        if (e.kind == RetrofitError.Kind.UNEXPECTED) throw e.cause!!
        val response = e.response
        if (response != null) {
            if (response.status == HttpURLConnection.HTTP_NOT_FOUND) {
                LOG.error(notFoundErrorMessage)
            } else {
                LOG.error("$baseErrorMessage. Response from server: ${response.status}")
            }
        } else {
            LOG.error("$baseErrorMessage: ${e.message}", e)
        }
    }

    private fun downloadFile(response: Response, targetPath: String): File? {
        val mimeType = response.body.mimeType()
        if (mimeType != "application/zip" && mimeType != "application/java-archive") return null

        var targetFile = File(targetPath)
        if (targetFile.isDirectory) {
            val guessFileName = guessFileName(response, response.url)
            if (guessFileName.contains(File.separatorChar)) {
                throw IOException("Invalid filename returned by a server")
            }
            val file = File(targetFile, guessFileName)
            if (file.parentFile != targetFile) {
                throw IOException("Invalid filename returned by a server")
            }
            targetFile = file
        }
        if (!targetFile.createNewFile()) {
            throw RuntimeException("Cannot create ${targetFile.absolutePath}")
        }
        targetFile.outputStream().use { response.body.`in`().copyTo(it) }
        LOG.info("Downloaded successfully to ${targetFile.absolutePath}")

        return targetFile
    }

    private fun guessFileName(response: Response, url: String): String {
        val filenameMarker = "filename="
        val contentDispositionHeader = response.headers.find { it.name.equals("Content-Disposition", true) }
        if (contentDispositionHeader == null || !contentDispositionHeader.value.contains(filenameMarker)) {
            val fileName = url.substringAfterLast('/')
            return if (fileName.isNotEmpty()) fileName else url
        }
        return contentDispositionHeader.value
                .substringAfter(filenameMarker, "")
                .substringBefore(';')
                .removeSurrounding("\"")
    }

    fun listPlugins(ideBuild: String, channel: String?, pluginId: String?): List<PluginBean> {
        val response = service.listPlugins(ideBuild, channel, pluginId)
        return response.categories?.flatMap { convertCategory(it) } ?: emptyList()
    }

    private fun convertCategory(response: RestCategoryBean): List<PluginBean> {
        return response.plugins?.map { convertPlugin(it, response.name!!) } ?: emptyList()
    }

    private fun convertPlugin(response: RestPluginBean, category: String): PluginBean {
        return PluginBean(
                response.name,
                response.id,
                response.version,
                category,
                response.ideaVersion.sinceBuild,
                response.ideaVersion.untilBuild,
                response.depends ?: emptyList()
        )
    }
}

private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")

private interface PluginRepositoryService {
    @Multipart
    @Headers("Accept: text/plain")
    @POST("/plugin/uploadPlugin")
    fun upload(@Part("userName") username: TypedString?, @Part("password") password: TypedString?,
               @Part("pluginId") pluginId: TypedString, @Part("channel") channel: TypedString?,
               @Part("file") file: TypedFile): Response

    @Multipart
    @Headers("Accept: text/plain")
    @POST("/plugin/uploadPlugin")
    fun uploadByXmlId(@Part("userName") username: TypedString?, @Part("password") password: TypedString?,
                      @Part("xmlId") pluginXmlId: TypedString, @Part("channel") channel: TypedString?,
                      @Part("file") file: TypedFile): Response


    @GET("/plugin/download")
    fun download(@Query("pluginId") pluginId: String, @Query("version") version: String,
                 @Query("channel") channel: String?): Response

    @GET("/pluginManager?action=download")
    fun downloadCompatiblePlugin(@Query("id") pluginId: String, @Query("build") ideBuild: String,
                                 @Query("channel") channel: String?): Response

    @GET("/plugins/list/")
    fun listPlugins(@Query("build") ideBuild: String,
                    @Query("channel") channel: String?,
                    @Query("pluginId") pluginId: String?): RestPluginRepositoryBean
}


val Response.text
    get() = this.body.`in`().reader().readText()