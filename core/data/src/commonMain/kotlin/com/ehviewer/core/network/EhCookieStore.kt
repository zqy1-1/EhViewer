package com.ehviewer.core.network

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url

object EhCookieStore : CookiesStorage {
    private val manager = getCookieManager()
    private val urlE = URLBuilder(URLProtocol.HTTPS, "e-hentai.org").build()
    private val urlEx = URLBuilder(URLProtocol.HTTPS, "exhentai.org").build()
    private val urlForums = URLBuilder(URLProtocol.HTTPS, "forums.e-hentai.org").build()

    fun removeAllCookies() = manager.removeAllCookies()

    fun hasSignedIn(): Boolean = manager.getCookies(urlE)?.run {
        containsKey(KEY_IPB_MEMBER_ID) && containsKey(KEY_IPB_PASS_HASH)
    } == true

    const val KEY_IPB_MEMBER_ID = "ipb_member_id"
    const val KEY_IPB_PASS_HASH = "ipb_pass_hash"
    const val KEY_IGNEOUS = "igneous"
    private const val KEY_HATH_PERKS = "hath_perks"
    private const val KEY_CONTENT_WARNING = "nw"
    private const val CONTENT_WARNING_NOT_SHOW = "1"
    private const val KEY_UTMP_NAME = "__utmp"

    /**
     * 10 years in seconds, used as the maxAge for imported identity cookies.
     *
     * A concrete value is required: a cookie with neither maxAge nor expires is
     * a session cookie, and android.webkit.CookieManager.flush() only persists
     * cookies that carry Max-Age/Expires. ktor renders any non-null maxAge as
     * "Max-Age=<value>" (see renderSetCookieHeader), so a moderate 10-year
     * value is used instead of Int.MAX_VALUE to stay clear of any range
     * clamping on the WebView side.
     */
    private const val COOKIE_MAX_AGE = 315_360_000 // 10 years
    private val sTipsCookie = Cookie(
        name = KEY_CONTENT_WARNING,
        value = CONTENT_WARNING_NOT_SHOW,
    )

    fun clearIgneous() {
        manager.setCookie(
            urlEx,
            Cookie(KEY_IGNEOUS, "", maxAge = 0, domain = urlEx.host, path = "/"),
        )
    }

    fun getUserId() = manager.getCookies(urlE)?.get(KEY_IPB_MEMBER_ID)

    fun getHathPerks() = manager.getCookies(urlE)?.get(KEY_HATH_PERKS)?.substringBefore('-')

    fun getIdentityCookies(): List<Pair<String, String?>> {
        val eCookies = manager.getCookies(urlE)
        val exCookies = manager.getCookies(urlEx)
        val ipbMemberId = eCookies?.get(KEY_IPB_MEMBER_ID)
        val ipbPassHash = eCookies?.get(KEY_IPB_PASS_HASH)
        val igneous = exCookies?.get(KEY_IGNEOUS)
        return listOf(
            KEY_IPB_MEMBER_ID to ipbMemberId,
            KEY_IPB_PASS_HASH to ipbPassHash,
            KEY_IGNEOUS to igneous,
        )
    }

    fun isCloudflareBypassed() = manager.getCookies(urlE)?.containsKey("cf_clearance") == true

    fun flush() = manager.flush()

    /**
     * Import identity cookies (ipb_member_id + ipb_pass_hash, optional igneous) for
     * cookie-based sign-in that bypasses Cloudflare. Mirrors the EhViewer_CN_SXJ
     * CookieSignInScene storeCookie() behaviour: writes to the e-hentai,
     * exhentai and forums hosts (SXJ parity) so every request in the app
     * carries the credentials. Uses COOKIE_MAX_AGE rather than a session
     * cookie so CookieManager.flush() persists the login across restarts.
     */
    fun importIdentityCookies(memberId: String, passHash: String, igneous: String = "") {
        clearAllIdentityCookies(clearIgneous = igneous.isNotEmpty())
        val domains = listOf(urlE, urlEx, urlForums)
        domains.forEach { url ->
            val domain = url.host
            manager.setCookie(url, Cookie(KEY_IPB_MEMBER_ID, memberId, maxAge = COOKIE_MAX_AGE, domain = domain, path = "/"))
            manager.setCookie(url, Cookie(KEY_IPB_PASS_HASH, passHash, maxAge = COOKIE_MAX_AGE, domain = domain, path = "/"))
            if (igneous.isNotEmpty()) {
                manager.setCookie(url, Cookie(KEY_IGNEOUS, igneous, maxAge = COOKIE_MAX_AGE, domain = domain, path = "/"))
            }
        }
        flush()
    }

    /**
     * Clear the stored identity cookies before an import.
     *
     * [clearIgneous] is false when the user imports without an igneous value:
     * an existing igneous (obtained via the WebView flow) stays valid, and
     * wiping it would only resurrect the exhentai sad-panda page. member_id and
     * pass_hash are always cleared so a stale identity cannot win over the new
     * one being written.
     */
    private fun clearAllIdentityCookies(clearIgneous: Boolean) {
        listOf(urlE, urlEx, urlForums).forEach { url ->
            manager.setCookie(url, Cookie(KEY_IPB_MEMBER_ID, "", maxAge = 0, domain = url.host, path = "/"))
            manager.setCookie(url, Cookie(KEY_IPB_PASS_HASH, "", maxAge = 0, domain = url.host, path = "/"))
            if (clearIgneous) {
                manager.setCookie(url, Cookie(KEY_IGNEOUS, "", maxAge = 0, domain = url.host, path = "/"))
            }
        }
        flush()
    }

    // See https://github.com/Ehviewer-Overhauled/Ehviewer/issues/873
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.name != KEY_UTMP_NAME) {
            manager.setCookie(requestUrl, cookie)
        }
    }

    override fun close() = Unit

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val checkTips = requestUrl.host == urlE.host
        return manager.getCookies(requestUrl)?.mapTo(mutableListOf()) {
            Cookie(it.key, it.value)
        }?.apply {
            if (checkTips) {
                add(sTipsCookie)
            }
        } ?: if (checkTips) listOf(sTipsCookie) else emptyList()
    }
}
