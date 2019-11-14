package org.jetbrains.intellij.pluginRepository

import com.google.gson.Gson
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
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
import retrofit.converter.Converter
import retrofit.converter.GsonConverter
import retrofit.converter.SimpleXMLConverter
import retrofit.http.*
import retrofit.mime.TypedFile
import retrofit.mime.TypedInput
import retrofit.mime.TypedOutput
import retrofit.mime.TypedString
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.nio.file.Files

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
        @param:Element(name = "vendor", required = false) @field:Element(required = false) val vendor: String?,
        @param:ElementList(entry = "depends", inline = true, required = false) @field:ElementList(entry = "depends", inline = true, required = false) val depends: List<String>? = null
)

data class RestError(val msg: String)

data class PluginInfoBean(
        val id: String,
        val name: String,
        val vendor: PluginVendorBean
)

data class PluginVendorBean(
        var name: String? = null,
        var url: String? = null
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
        val vendor: String?,
        val depends: List<String>
)

class CompositeConverter : Converter {
    private val xmlConverter = SimpleXMLConverter()
    private val jsonConverter = GsonConverter(Gson())

    override fun fromBody(body: TypedInput, type: Type?): Any = when {
        body.mimeType().startsWith("text/xml") -> {
            xmlConverter.fromBody(body, type)
        }
        body.mimeType().startsWith("application/json") -> {
            jsonConverter.fromBody(body, type)
        }
        else -> {
            val outputStream = ByteOutputStream()
            body.`in`().copyTo(outputStream)
            outputStream.toString()
        }
    }

    override fun toBody(`object`: Any?): TypedOutput {
        return xmlConverter.toBody(`object`)
    }
}

/**
 * @author nik
 * @param siteUrl url of plugins repository instance. For example: https://plugins.jetbrains.com
 * @param token hub [permanent token](https://www.jetbrains.com/help/hub/Manage-Permanent-Tokens.html) to be used for authorization
 */
class PluginRepositoryInstance constructor(val siteUrl: String, private val token: String? = null) {
    private val service = RestAdapter.Builder()
            .setEndpoint(siteUrl)
            .setClient { ->
                object : UrlConnectionClient() {
                    override fun openConnection(request: Request?): HttpURLConnection {
                        val connection = super.openConnection(request)
                        val timeout = 10 * 60 * 1000
                        connection.readTimeout = timeout
                        return connection
                    }
                }
            }
            .setRequestInterceptor { request ->
                request.addHeader("Authorization", "Bearer $token")
            }
            .setLog { LOG.debug(it) }
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setConverter(CompositeConverter())
            .build()
            .create(PluginRepositoryService::class.java)

    @JvmOverloads
    fun uploadPlugin(pluginId: Int, file: File, channel: String? = null, notes: String? = null) {
        uploadPluginInternal(file, pluginId = pluginId, channel = channel, notes = notes)
    }

    @JvmOverloads
    fun uploadPlugin(pluginXmlId: String, file: File, channel: String? = null, notes: String? = null) {
        uploadPluginInternal(file, pluginXmlId = pluginXmlId, channel = channel, notes = notes)
    }

    fun uploadNewPlugin(file: File, family: String, categoryId: Int, licenseUrl: String): PluginInfoBean {
        ensureCredentialsAreSet()
        try {
            LOG.info("Uploading new plugin from ${file.absolutePath}")
            val uploadedPlugin = service.uploadNewPlugin(file.toTypedFile(), family, TypedString(licenseUrl), TypedString(categoryId.toString()))
            LOG.info("${uploadedPlugin.name} was successfully uploaded with id ${uploadedPlugin.id}")
            return uploadedPlugin
        } catch (e: RetrofitError) {
            throw UploadFailedException(processRetofitError(e, "", "Failed to upload plugin"), e)
        }
    }

    private fun uploadPluginInternal(file: File, pluginId: Int? = null, pluginXmlId: String? = null, channel: String? = null, notes: String? = null) {
        ensureCredentialsAreSet()
        try {
            LOG.info("Uploading plugin ${pluginXmlId ?: pluginId} from ${file.absolutePath} to $siteUrl")
            val response = if (pluginXmlId != null) {
                service.uploadByXmlId(TypedString(pluginXmlId), channel?.let { TypedString(it) }, notes?.let { TypedString(it) }, file.toTypedFile())
            } else {
                service.upload(TypedString(pluginId.toString()), channel?.let { TypedString(it) }, notes?.let { TypedString(it) }, file.toTypedFile())
            }
            LOG.info("Done: " + response.text)
        } catch (e: RetrofitError) {
            val notFoundErrorMessage = "Cannot find $pluginXmlId. " +
                    "Note that you need to upload the plugin to the repository at least once manually " +
                    "(to specify options like the license, repository URL etc.) before uploads through the client can be used."
            val errorMessage = processRetofitError(e, notFoundErrorMessage, "Failed to upload plugin")
            throw UploadFailedException(errorMessage, e)
        }
    }

    private fun ensureCredentialsAreSet() {
        if (token == null) {
            throw RuntimeException("Token must be set for uploading")
        }
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

    private fun processRetofitError(e: RetrofitError, notFoundErrorMessage: String, baseErrorMessage: String): String {
        //see `retrofit.RetrofitError.Kind.UNEXPECTED` doc
        if (e.kind == RetrofitError.Kind.UNEXPECTED) throw e.cause!!
        val errorMessage = retrofitErrorMessage(e, baseErrorMessage, notFoundErrorMessage)
        LOG.error(errorMessage, e)
        return errorMessage
    }

    private fun retrofitErrorMessage(e: RetrofitError, baseErrorMessage: String, notFoundErrorMessage: String): String {
        val response = e.response ?: return "$baseErrorMessage: ${e.message}"
        if (response.status == HttpURLConnection.HTTP_NOT_FOUND) {
            return notFoundErrorMessage
        }
        if (response.body.mimeType().startsWith("application/json")) {
            val bodyAs = e.getBodyAs(RestError::class.java)
            if (bodyAs is RestError) {
                return bodyAs.msg
            }
        }
        if (response.body.mimeType().startsWith("text/plain")) {
            return e.body.toString()
        }
        return "$baseErrorMessage. Response from server: ${response.status}"
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
        Files.copy(response.body.`in`(), targetFile.toPath())
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

    fun pluginInfo(family: String, pluginXmlId: String): PluginInfoBean? {
        return try {
            service.pluginInfo(family, pluginXmlId)
        } catch (e: RetrofitError) {
            processRetofitError(e, "Cannot find $pluginXmlId in $family family", "Can't get plugin info")
            null
        }
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
                response.vendor,
                response.depends ?: emptyList()
        )
    }
}

private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")

private interface PluginRepositoryService {
    @Multipart
    @Headers("Accept: text/plain")
    @POST("/plugin/uploadPlugin")
    fun upload(@Part("pluginId") pluginId: TypedString,
               @Part("channel") channel: TypedString?,
               @Part("notes") notes: TypedString?,
               @Part("file") file: TypedFile): Response

    @Multipart
    @Headers("Accept: text/plain")
    @POST("/plugin/uploadPlugin")
    fun uploadByXmlId(@Part("xmlId") pluginXmlId: TypedString,
                      @Part("channel") channel: TypedString?,
                      @Part("notes") notes: TypedString?,
                      @Part("file") file: TypedFile): Response

    @Multipart
    @POST("/api/plugins/{family}/upload")
    fun uploadNewPlugin(@Part("file") file: TypedFile,
                        @Path("family") family: String,
                        @Part("licenseUrl") licenseUrl: TypedString,
                        @Part("cid") category: TypedString): PluginInfoBean


    @Streaming
    @GET("/plugin/download")
    fun download(@Query("pluginId") pluginId: String, @Query("version") version: String,
                 @Query("channel") channel: String?): Response

    @Streaming
    @GET("/pluginManager?action=download")
    fun downloadCompatiblePlugin(@Query("id") pluginId: String, @Query("build") ideBuild: String,
                                 @Query("channel") channel: String?): Response

    @GET("/plugins/list/")
    fun listPlugins(@Query("build") ideBuild: String,
                    @Query("channel") channel: String?,
                    @Query("pluginId") pluginId: String?): RestPluginRepositoryBean

    @GET("/api/plugins/{family}/{pluginXmlId}")
    fun pluginInfo(@Path("family") family: String, @Path("pluginXmlId") pluginXmlId: String): PluginInfoBean
}

private val Response.text
    get() = this.body.`in`().reader().readText()

private fun File.toTypedFile(): TypedFile = TypedFile("application/octet-stream", this)