package com.hippo.ehviewer.dns

import java.net.InetAddress

/**
 * Fallback address table for the hosts the app needs when the system resolver
 * is poisoned.
 *
 * These are the real addresses as returned by DoH (doh.pub / 1.12.12.12) and
 * they match the table EhViewer_CN_SXJ ships. They go stale as the CDN
 * rotates -- that is why DNS-over-HTTPS is tried first when enabled, and why
 * this table is only a fallback. Update it by resolving the names over a
 * working DoH endpoint.
 */
object BuiltInHosts {
    private val table: Map<String, List<String>> = mapOf(
        "e-hentai.org" to listOf(
            "172.66.140.62",
            "172.66.132.196",
        ),
        "repo.e-hentai.org" to listOf(
            "172.66.140.62",
            "172.66.132.196",
        ),
        "forums.e-hentai.org" to listOf(
            "172.66.132.196",
            "172.66.140.62",
        ),
        "exhentai.org" to listOf(
            "172.67.187.219",
            "104.21.56.202",
        ),
        "ehgt.org" to listOf(
            "109.236.85.28",
            "89.39.106.43",
            "62.112.8.21",
        ),
        "s.exhentai.org" to listOf(
            "199.59.148.97",
        ),
        "upld.e-hentai.org" to listOf(
            "95.211.208.236",
            "89.149.221.236",
        ),
        "upld.exhentai.org" to listOf(
            "178.175.132.22",
            "178.175.129.254",
            "178.175.128.254",
        ),
    )

    private val cache = HashMap<String, List<InetAddress>>()
    private val misses = HashSet<String>()

    /**
     * Returns the pinned addresses for [hostname], or null when the host is
     * not covered by the table or none of its entries parse. Results are
     * memoised, including misses, so a miss costs one set lookup.
     */
    fun lookup(hostname: String): List<InetAddress>? = synchronized(cache) {
        if (misses.contains(hostname)) return null
        cache[hostname]?.let { return it }

        val resolved = table[hostname].orEmpty().mapNotNull { literal ->
            runCatching { InetAddress.getByName(literal) }.getOrNull()
        }
        if (resolved.isEmpty()) {
            misses.add(hostname)
            return null
        }
        cache[hostname] = resolved
        return resolved
    }
}
