package org.jetbrains.intellij.pluginRepository

import retrofit.RestAdapter
import java.io.File
import retrofit.http.POST
import retrofit.mime.TypedString
import retrofit.client.Response
import retrofit.http.Multipart
import retrofit.http.Part
import retrofit.mime.TypedFile
import retrofit.client.UrlConnectionClient
import retrofit.client.Request
import java.net.HttpURLConnection
import retrofit.RetrofitError

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
            logLine("Uploading plugin $pluginId from ${file.getAbsolutePath()} to $siteUrl")
            service.upload(TypedString(username), TypedString(password), TypedString(pluginId.toString()),
                    channel?.let { TypedString(it) },
                    TypedFile("application/octet-stream", file))
        }
        catch(e: RetrofitError) {
            if (e.getResponse()?.getStatus() == 302) {
                logLine("Uploaded successfully")
                return
            }
            throw e;
        }
    }
}

private fun logLine(s: String) {
    println(s)
}

private trait PluginRepositoryService {
    Multipart
    POST("/plugin/uploadPlugin")
    fun upload(Part("userName") username: TypedString, Part("password") password: TypedString,
               Part("pluginId") pluginId: TypedString, Part("channel") channel: TypedString?,
               Part("file") file: TypedFile): Response
}