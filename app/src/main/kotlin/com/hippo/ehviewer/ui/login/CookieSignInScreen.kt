package com.hippo.ehviewer.ui.login

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ehviewer.core.i18n.R
import com.ehviewer.core.network.EhCookieStore
import com.hippo.ehviewer.ui.Screen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import splitties.systemservices.clipboardManager

/**
 * Cookie-based sign-in screen that lets the user paste login cookies
 * (ipb_member_id, ipb_pass_hash, optionally igneous) to bypass Cloudflare
 * without going through the browser flow. Mirrors the EhViewer_CN_SXJ
 * CookieSignInScene behaviour.
 *
 * Cookie format (one per line, "name: value"):
 *   ipb_member_id: 12345
 *   ipb_pass_hash: abc123...
 *   igneous: xyz789...
 */
@Destination<RootGraph>
@Composable
fun AnimatedVisibilityScope.CookieSignInScreen(navigator: DestinationsNavigator) = Screen(navigator) {
    val context = LocalContext.current
    val cookieInput = rememberTextFieldState()
    var cookieError by mutableStateOf<String?>(null)
    val noDataInClipboard = stringResource(R.string.cookie_sign_in_no_data)
    val invalidFormat = stringResource(R.string.cookie_sign_in_failed)

    fun importFromClipboard() {
        try {
            val clip = clipboardManager.getPrimaryClip()
            val text = clip?.getItemAt(0)?.coerceToText(context)?.toString()
            if (!text.isNullOrBlank()) {
                cookieInput.setTextAndPlaceCursorAtEnd(text)
                cookieError = null
            } else {
                cookieError = noDataInClipboard
            }
        } catch (_: SecurityException) {
            cookieError = noDataInClipboard
        }
    }

    /**
     * Parse pasted cookie text. Accepts the formats users actually copy:
     *
     *   1. one "name: value" per line (the layout shown in the hint)
     *   2. one "name=value" per line
     *   3. a raw Cookie request header: "a=1; b=2; c=3" (single or multi-line)
     *
     * Recognised names: ipb_member_id, ipb_pass_hash, igneous. Anything else
     * (session ids, cf_clearance, ...) is ignored rather than rejected, so a
     * full header paste still works. Returns (memberId, passHash, igneous) or
     * null when memberId or passHash is missing.
     */
    fun parseCookies(text: String): Triple<String, String, String>? {
        var memberId = ""
        var passHash = ""
        var igneous = ""

        // Split on newlines and on ';' (Cookie header separator), then split
        // each token on the first ':' or '=' whichever comes first.
        text.split('\n', ';').forEach { raw ->
            val token = raw.trim().removePrefix("Cookie:").trim()
            if (token.isEmpty()) return@forEach
            val sepIndex = token.indexOfFirst { it == ':' || it == '=' }
            if (sepIndex <= 0) return@forEach
            val name = token.substring(0, sepIndex).trim()
            val value = token.substring(sepIndex + 1).trim().trim('"')
            when (name) {
                EhCookieStore.KEY_IPB_MEMBER_ID -> memberId = value
                EhCookieStore.KEY_IPB_PASS_HASH -> passHash = value
                EhCookieStore.KEY_IGNEOUS -> igneous = value
            }
        }

        if (memberId.isEmpty() || passHash.isEmpty()) return null
        return Triple(memberId, passHash, igneous)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(dimensionResource(id = com.hippo.ehviewer.R.dimen.keyline_margin)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.cookie_sign_in_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            state = cookieInput,
            modifier = Modifier.width(dimensionResource(id = com.hippo.ehviewer.R.dimen.single_max_width)),
            label = { Text(text = stringResource(R.string.cookie_sign_in_tip)) },
            supportingText = cookieError?.let {
                { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
            isError = cookieError != null,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            TextButton(
                onClick = ::importFromClipboard,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = stringResource(R.string.cookie_sign_in_import),
                    textDecoration = TextDecoration.Underline,
                )
            }
            TextButton(
                onClick = {
                    val parsed = parseCookies(cookieInput.text.toString())
                    if (parsed == null) {
                        cookieError = invalidFormat
                        return@TextButton
                    }
                    val (memberId, passHash, igneous) = parsed
                    // Do NOT call EhUtils.signOut() here: it sets needSignIn=true
                    // which yanks the user back to the password screen mid-login,
                    // and it would wipe cf_clearance. importIdentityCookies()
                    // already clears the old identity cookies itself.
                    EhCookieStore.importIdentityCookies(memberId, passHash, igneous)
                    postLogin()
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = stringResource(R.string.cookie_sign_in_title),
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}
