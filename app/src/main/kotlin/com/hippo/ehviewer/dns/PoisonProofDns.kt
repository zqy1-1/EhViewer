package com.hippo.ehviewer.dns

import com.hippo.ehviewer.Settings
import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.Dns

/**
 * DNS resolution that can bypass DNS poisoning of the E-Hentai domains.
 *
 * On a poisoned resolver e-hentai.org resolves to addresses belonging to
 * unrelated services (Twitter, Facebook), so every connection fails at the
 * TLS handshake. Two layers are tried in order, both optional:
 *
 *  1. a built-in address table [BuiltInHosts] -- works offline and costs
 *     nothing, but the addresses age and need periodic updates;
 *  2. DNS-over-HTTPS [DohResolver] -- always current, requires the resolver
 *     itself to be reachable.
 *
 * Anything not covered falls through to the system resolver, so this stays
 * transparent for every other host the app talks to.
 */
class PoisonProofDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        if (Settings.builtInHosts.value) {
            BuiltInHosts.lookup(hostname)?.let { return it }
        }
        if (Settings.doh.value) {
            DohResolver.lookup(hostname).takeIf { it.isNotEmpty() }?.let { return it }
        }
        return try {
            InetAddress.getAllByName(hostname).toList()
        } catch (e: NullPointerException) {
            // Broken system behaviour, same guard the original EhHosts has.
            throw UnknownHostException("Broken system behaviour for DNS lookup of $hostname").apply {
                initCause(e)
            }
        }
    }
}
