package org.ireader.domain.feature_services.io.coil

import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.source
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.domain.feature_services.io.LibraryCovers
import tachiyomi.core.http.okhttp
import tachiyomi.core.io.saveTo
import tachiyomi.source.HttpSource
import java.io.File
import java.util.*

internal class LibraryMangaFetcher(
    private val defaultClient: OkHttpClient,
    private val libraryCovers: LibraryCovers,
    private val getLocalCatalog: GetLocalCatalog,
    private val coilCache: Cache,
) : Fetcher<BookCover> {

    override fun key(data: BookCover): String? {
        return when (getResourceType(data.cover)) {
            Type.File -> {
                val cover = File(data.cover.substringAfter("file://"))
                "${data.cover}_${cover.lastModified()}"
            }
            Type.URL -> {
                val cover = libraryCovers.find(data.id).toFile()
                if (data.favorite && (!cover.exists() || cover.lastModified() == 0L)) {
                    null
                } else {
                    "${data.cover}_${cover.lastModified()}"
                }
            }
            null -> null
        }
    }

    override suspend fun fetch(
        pool: BitmapPool,
        data: BookCover,
        size: Size,
        options: Options,
    ): SourceResult {
        return when (getResourceType(data.cover)) {
            Type.File -> getFileLoader(data)
            Type.URL -> getUrlLoader(data)
            null -> error("Not a valid image")
        }
    }

    private fun getFileLoader(manga: BookCover): SourceResult {
        val file = File(manga.cover.substringAfter("file://"))
        return getFileLoader(file)
    }

    private fun getFileLoader(file: File): SourceResult {
        return SourceResult(
            source = file.source().buffer(),
            mimeType = "image/*",
            dataSource = DataSource.DISK
        )
    }

    private suspend fun getUrlLoader(manga: BookCover): SourceResult {
        val file = libraryCovers.find(manga.id).toFile()
        if (file.exists() && file.lastModified() != 0L) {
            return getFileLoader(file)
        }

        val call = getCall(manga)

        // TODO this crashes if using suspending call due to a compiler bug
        val response = withContext(Dispatchers.IO) { call.execute() }
        val body = checkNotNull(response.body) { "Response received null body" }

        if (manga.favorite) {
            // If the cover isn't already saved or the size is different, save it
            if (!file.exists() || file.length() != body.contentLength()) {
                val tmpFile = File(file.absolutePath + ".tmp")
                try {
                    body.source().saveTo(tmpFile)
                    tmpFile.renameTo(file)
                } finally {
                    tmpFile.delete()
                }
                return getFileLoader(file)
            }
            // If the cover is already saved but both covers have the same size, use the saved one
            if (file.exists() && file.length() == body.contentLength()) {
                body.close()
                file.setLastModified(Calendar.getInstance().timeInMillis)
                return getFileLoader(file)
            }
        }

        // Fallback to image from source
        return SourceResult(
            source = body.source(),
            mimeType = "image/*",
            dataSource = if (response.cacheResponse != null) DataSource.DISK else DataSource.NETWORK
        )
    }

    private fun getCall(manga: BookCover): Call {
        val catalog = getLocalCatalog.get(manga.sourceId)
        val source = catalog?.source as? HttpSource

        val clientAndRequest = source?.getCoverRequest(manga.cover)

        val newClient = (clientAndRequest?.first?.okhttp ?: defaultClient).newBuilder()
            .cache(coilCache)
            .build()

        val request = clientAndRequest?.second?.build()?.convertToOkHttpRequest()
            ?: Request.Builder().url(manga.cover).build()

        return newClient.newCall(request)
    }

    private fun getResourceType(cover: String): Type? {
        return when {
            cover.isEmpty() -> null
            cover.startsWith("http") -> Type.URL
            cover.startsWith("/") || cover.startsWith("file://") -> Type.File
            else -> null
        }
    }

    private enum class Type {
        File, URL;
    }

}

/**
 * Converts a ktor request to okhttp. Note that it does not support sending a request body. If we
 * ever need it we could use reflection to call this other method instead:
 * https://github.com/ktorio/ktor/blob/1.6.4/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/OkHttpEngine.kt#L180
 */
@OptIn(InternalAPI::class)
private fun HttpRequestData.convertToOkHttpRequest(): Request {
    val builder = Request.Builder()

    with(builder) {
        url(url.toString())
        mergeHeaders(headers, body) { key, value ->
            if (key == HttpHeaders.ContentLength) return@mergeHeaders
            addHeader(key, value)
        }

        method(method.value, null)
    }

    return builder.build()
}
