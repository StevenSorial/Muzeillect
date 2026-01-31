package com.steven.muzeillect

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.steven.muzeillect.utils.BASE_URL
import com.steven.muzeillect.utils.isValidImage
import com.steven.muzeillect.utils.useOrNull
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.coroutines.executeAsync
import okhttp3.internal.closeQuietly
import okhttp3.tls.HandshakeCertificates
import org.jsoup.Jsoup
import timber.log.Timber
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object NetworkClient {

  private lateinit var client: OkHttpClient

  lateinit var imageLoader: ImageLoader
    private set

  fun initClients(context: Context) {
    if (::client.isInitialized && ::imageLoader.isInitialized) return

    val cacheSize = 100L * 1024 * 1024 // 100 MB
    val cacheDir = context.cacheDir.resolve("http_cache")

    client = OkHttpClient.Builder()
      .cache(Cache(cacheDir, cacheSize))
      .connectTimeout(30.seconds)
      .addNetworkInterceptor(CacheInterceptor())
      .readTimeout(2.minutes)
      .addFallbackCertificateIfNeeded(context)
      .build()

    imageLoader = ImageLoader.Builder(context)
      .components {
        add(OkHttpNetworkFetcherFactory(callFactory = { client }))
      }
      .build()
  }

  suspend fun getMaxToken(): Long {
    var response: Response? = null
    try {
      val req = Request.Builder().url(BASE_URL.toString()).build()
      response = client.newCall(req).executeAsync()
      val docString = response.body.string()
      val doc = Jsoup.parse(docString)
      val element = doc.select("a.post").first()!!.attributes().asList()[1].value
        .removePrefix("/")
      return element.toLong()
    } finally {
      response?.closeQuietly()
    }
  }

  suspend fun getImageUrl(tokenUrl: Uri): Uri {

    var response: Response? = null
    try {
      val req = Request.Builder().url(tokenUrl.toString()).build()
      response = client.newCall(req).executeAsync()
      val doc = Jsoup.parse(response.body.string())
      val img = doc.select("#ii").first()
      return img!!.attr("src").toUri()
    } finally {
      response?.closeQuietly()
    }
  }

  suspend fun <T> getImageData(url: Uri, handler: suspend (response: Response) -> T): T {
    var response: Response? = null
    try {
      val req = Request.Builder().url(url.toString()).build()
      response = client.newCall(req).executeAsync()
      return handler(response)
    } finally {
      response?.closeQuietly()
    }
  }
}

private class CacheInterceptor() : Interceptor {

  companion object {
    private val ARCHILLECT_URL_REGEX = Regex(
      """^https://archillect\.com/(\d+)/?$""",
      RegexOption.IGNORE_CASE
    )
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (request.method != "GET" || !response.isSuccessful) {
      return response
    }

    Timber.d("intercepting ${request.url}")
    val age = cacheAge(request.url.toString().toUri()) ?: return response
    Timber.d("Adding cache age of  ${age.toString(DurationUnit.DAYS, decimals = 2)}")

    return response.newBuilder()
      .removeHeader("Pragma")
      .header("Cache-Control", "public, max-age=${age.inWholeSeconds}")
      .build()
  }

  fun cacheAge(url: Uri): Duration? {
    if (url.isValidImage) {
      return 30.days
    }
    val token = ARCHILLECT_URL_REGEX.matchEntire(url.toString())
      ?.groupValues
      ?.getOrNull(1)
      ?.toLongOrNull()
      ?: return null
    // Consider the token number as minutes for cache duration. A way to randomize cache age
    return token.toDuration(DurationUnit.MINUTES)
  }
}

private fun OkHttpClient.Builder.addFallbackCertificateIfNeeded(context: Context): OkHttpClient.Builder {

  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) return this

  val cf = CertificateFactory.getInstance("X.509")

  val certificateStream = context.resources.openRawResource(R.raw.isrgrootx1)
  val certificate = certificateStream.useOrNull {
    cf.generateCertificate(it)
  } ?: return this

  val handshakeCertificate = HandshakeCertificates.Builder()
    .addTrustedCertificate(certificate as X509Certificate)
    .addPlatformTrustedCertificates()
    .build()

  return sslSocketFactory(
    handshakeCertificate.sslSocketFactory(),
    handshakeCertificate.trustManager
  )
}
