package com.steven.muzeillect

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.steven.muzeillect.utils.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.coroutines.executeAsync
import okhttp3.internal.closeQuietly
import okhttp3.tls.HandshakeCertificates
import org.jsoup.Jsoup
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object NetworkClient {

  private lateinit var client: OkHttpClient

  lateinit var imageLoader: ImageLoader
    private set

  fun initClients(context: Context) {
    if (::client.isInitialized && ::imageLoader.isInitialized) return


    var builder = OkHttpClient.Builder()
      .connectTimeout(30.seconds)
      .readTimeout(2.minutes)

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
      val certificates = getFallbackCertificate(context)
      builder = builder.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
    }

    client = builder.build()

    imageLoader = ImageLoader.Builder(context)
      .components {
        add(OkHttpNetworkFetcherFactory(callFactory = { client }))
      }
      .build()
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

  suspend fun <T> getImageData(url: Uri, handler: suspend (response: Response) -> T): T {
    var response: Response? = null
    try {
      val req = Request.Builder().url(url.toString()).build()
      response = client.newCall(req).executeAsync()
      return handler(response)
    }finally {
      response?.closeQuietly()
    }
  }
}

private fun getFallbackCertificate(context: Context): HandshakeCertificates {
  val cf = CertificateFactory.getInstance("X.509")

  val certificateStream = context.resources.openRawResource(R.raw.isrgrootx1)
  val certificate = certificateStream.use {
    cf.generateCertificate(it)
  }

  return HandshakeCertificates.Builder()
    .addTrustedCertificate(certificate as X509Certificate)
    .addPlatformTrustedCertificates()
    .build()
}
