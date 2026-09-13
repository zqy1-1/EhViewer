package com.hippo.ehviewer.dns

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Domain fronting: connect by IP and skip sending SNI, but only for the hosts
 * that need it.
 *
 * A TLS ClientHello carrying an E-Hentai host name in its SNI extension is
 * reset by the interceptor before the handshake completes, which surfaces as
 * "SSL handshake aborted: Connection reset by peer". Passing an [InetAddress]
 * instead of the host name makes the platform omit SNI while the certificate
 * is still validated through [trustManager].
 *
 * This applies only to the Cloudflare-fronted hosts in [FRONTABLE_HOSTS].
 * Omitting SNI against an origin server (api., upld., s.) breaks it the other
 * way round: those hosts have no other way to learn which site is wanted, so
 * the request hangs until it times out. Everything else is delegated to the
 * platform factory unchanged.
 */
class DomainFrontingSslSocketFactory(
    private val trustManager: X509TrustManager,
) : SSLSocketFactory() {
    private val delegate: SSLSocketFactory = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustManager), null)
    }.socketFactory

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    override fun createSocket(
        s: Socket,
        host: String,
        port: Int,
        autoClose: Boolean,
    ): Socket {
        if (!com.hippo.ehviewer.Settings.domainFronting.value || !isFrontable(host)) {
            return delegate.createSocket(s, host, port, autoClose)
        }
        val address: InetAddress? = s.inetAddress
        if (address == null) {
            return delegate.createSocket(s, host, port, autoClose)
        }
        if (autoClose) {
            runCatching { s.close() }
        }
        return delegate.createSocket(address, port)
    }

    override fun createSocket(host: String, port: Int): Socket = delegate.createSocket(host, port)

    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int,
    ): Socket = delegate.createSocket(host, port, localHost, localPort)

    override fun createSocket(host: InetAddress, port: Int): Socket = delegate.createSocket(host, port)

    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket = delegate.createSocket(address, port, localAddress, localPort)

    /** Convenience overload used by OkHttp on some code paths. */
    @Throws(IOException::class)
    fun createSocket(host: String, port: Int, autoClose: Boolean): Socket =
        delegate.createSocket(host, port)

    private fun isFrontable(host: String): Boolean =
        FRONTABLE_HOSTS.any { host == it || host.endsWith(".$it") }

    companion object {
        /**
         * Hosts behind Cloudflare, which is where the SNI reset happens and
         * where omitting SNI still works (the Host header routes the request).
         * Origin servers are deliberately absent.
         */
        private val FRONTABLE_HOSTS = listOf(
            "e-hentai.org",
            "exhentai.org",
        )
    }
}
