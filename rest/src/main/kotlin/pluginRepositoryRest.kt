package org.jetbrains.intellij.pluginRepository

import org.slf4j.LoggerFactory
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Request
import retrofit.client.Response
import retrofit.client.UrlConnectionClient
import retrofit.http.Multipart
import retrofit.http.POST
import retrofit.http.Part
import retrofit.mime.TypedFile
import retrofit.mime.TypedString
import java.io.File
import java.net.HttpURLConnection

/**
 * @author nik
 */
public class PluginRepositoryInstance(val siteUrl: String, private val username: String, private val password: String) {
    private val service = RestAdapter.Builder()
            .setEndpoint(siteUrl)
            .setClient({ -> object: UrlConnectionClient() {
                override fun openConnection(request: Request?): HttpURLConnection {
                    val connection = super.openConnection(request)
                    connection.setInstanceFollowRedirects(false)
                    val timeout = 10 * 60 * 1000
                    connection.setReadTimeout(timeout)
                    return connection
                }
            }})
            .build()
            .create(javaClass<PluginRepositoryService>())

    fun uploadPlugin(pluginId: Int, file: File, channel: String? = null) {
        try {
            LOG.info("Uploading plugin $pluginId from ${file.getAbsolutePath()} to $siteUrl")
            service.upload(TypedString(username), TypedString(password), TypedString(pluginId.toString()),
                    channel?.let { TypedString(it) },
                    TypedFile("application/octet-stream", file))
        }
        catch(e: RetrofitError) {
            if (e.getResponse()?.getStatus() == 302) {
                LOG.info("Uploaded successfully")
                return
            }
            throw e;
        }
    }
}

private val LOG = LoggerFactory.getLogger("plugin-repository-rest-client")

private interface PluginRepositoryService {
    Multipart
    POST("/plugin/uploadPlugin")
    fun upload(Part("userName") username: TypedString, Part("password") password: TypedString,
               Part("pluginId") pluginId: TypedString, Part("channel") channel: TypedString?,
               Part("file") file: TypedFile): Response
}