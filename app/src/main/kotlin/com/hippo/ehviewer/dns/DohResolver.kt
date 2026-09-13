package com.hippo.ehviewer.dns

import com.hippo.ehviewer.Settings
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps

/**
 * DNS-over-HTTPS resolution used to defeat DNS poisoning of the E-Hentai
 * domains. The resolver itself is addressed by IP so it cannot be poisoned.
 *
 * An isolated OkHttp client is used on purpose: it must not share the app's
 * connection pool or its [PoisonProofDns] instance, otherwise the resolver
 * would depend on the very resolution it is meant to fix.
 */
object DohResolver : Dns {
    // doh.pub and 1.12.12.12 return the real addresses for the E-Hentai
    // domains; several other resolvers reachable from CN still hand back
    // poisoned answers.
    private const val DEFAULT_ENDPOINT = "https://1.12.12.12/dns-query"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val resolver by lazy {
        val endpoint = Settings.dohEndpoint.value.ifBlank { DEFAULT_ENDPOINT }
        DnsOverHttps.Builder()
            .client(client)
            .url(endpoint.toHttpUrlOrNull() ?: DEFAULT_ENDPOINT.toHttpUrl())
            .post(true)
            .build()
    }

    /** Queries the name; an empty result means "not resolved, fall through". */
    override fun lookup(hostname: String): List<InetAddress> =
        runCatching { resolver.lookup(hostname) }.getOrNull().orEmpty()
}
