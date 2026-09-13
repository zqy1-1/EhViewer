package com.hippo.ehviewer.dns

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

/**
 * Domain fronting: connect by IP and skip sending SNI.
 *
 * A TLS ClientHello that carries an E-Hentai host name in its SNI extension is
 * reset by the interceptor before the handshake completes, which surfaces as
 * "SSL handshake aborted: Connection reset by peer". Handing the factory an
 * [InetAddress] instead of the host name makes the platform omit SNI while the
 * certificate is still validated by the normal trust chain and the Host header
 * still tells the server which site is wanted.
 *
 * Only used when [com.hippo.ehviewer.Settings.domainFronting] is on; otherwise
 * every call delegates to the platform factory unchanged.
 */
class DomainFrontingSslSocketFactory(
    private val delegate: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory,
) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    override fun createSocket(
        s: Socket,
        host: String,
        port: Int,
        autoClose: Boolean,
    ): Socket {
        if (!com.hippo.ehviewer.Settings.domainFronting.value) {
            return delegate.createSocket(s, host, port, autoClose)
        }
        val address: InetAddress? = s.inetAddress
        if (autoClose) {
            runCatching { s.close() }
        }
        // Passing an InetAddress makes the platform connect without SNI.
        return if (address != null) {
            delegate.createSocket(address, port)
        } else {
            delegate.createSocket(s, host, port, autoClose)
        }
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

    /** Not part of SSLSocketFactory's abstract API on all platforms; kept for completeness. */
    @Throws(IOException::class)
    fun createSocket(host: String, port: Int, autoClose: Boolean): Socket =
        delegate.createSocket(host, port)
}
