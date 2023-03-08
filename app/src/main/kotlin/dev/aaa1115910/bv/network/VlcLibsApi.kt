package dev.aaa1115910.bv.network

import android.os.Build
import dev.aaa1115910.bv.network.entity.ReleaseItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.content.ProgressListener
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.lang.IllegalStateException

object VlcLibsApi {
    private var endPoint = "api.github.com"
    private lateinit var client: HttpClient
    private val logger = KotlinLogging.logger { }

    init {
        createClient()
    }

    private fun createClient() {
        client = HttpClient(OkHttp) {
            BrowserUserAgent()
            install(ContentNegotiation) {
                json(Json {
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(ContentEncoding) {
                deflate(1.0F)
                gzip(0.9F)
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = endPoint
                }
            }
        }
    }

    suspend fun getReleases(): List<ReleaseItem> {
        val result = mutableListOf<ReleaseItem>()

        runCatching {
            result.addAll(
                client.get("/repos/aaa1115910/bv-libs/releases").body<List<ReleaseItem>>()
            )
        }

        return result
    }

    suspend fun getRelease(vlcVersion: String): ReleaseItem? {
        return getReleases().firstOrNull { it.tagName == "libvlc-${vlcVersion}" }
    }

    suspend fun downloadFile(
        releaseItem: ReleaseItem,
        file: File,
        downloadListener: ProgressListener
    ) {
        val fileName = getFileName()
        if (fileName == "") throw IllegalStateException("Not supported abi")

        val downloadUrl = releaseItem.assets
            .firstOrNull { it.name == fileName }?.browserDownloadUrl
            ?: throw IllegalStateException("Not found download url")
        client.prepareRequest {
            url(downloadUrl)
            onDownload(downloadListener)
        }.execute { response ->
            response.bodyAsChannel().copyAndClose(file.writeChannel())
        }
    }

    private fun getFileName(): String {
        if (Build.SUPPORTED_ABIS.contains("arm64-v8a")) {
            return "arm64-v8a.zip"
        } else if (Build.SUPPORTED_ABIS.contains("armeabi-v7a")) {
            return "armeabi-v7a.zip"
        } else if (Build.SUPPORTED_ABIS.contains("x86_64")) {
            return "x86_64.zip"
        } else if (Build.SUPPORTED_ABIS.contains("x86")) {
            return "x86.zip"
        } else {
            return ""
        }
    }
}

