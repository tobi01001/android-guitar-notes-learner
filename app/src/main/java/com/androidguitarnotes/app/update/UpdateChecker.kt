package com.androidguitarnotes.app.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Information about a published GitHub release. */
data class ReleaseInfo(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String,
)

/** Checks the latest GitHub release and compares it to the installed version. */
object UpdateChecker {
    const val REPOSITORY = "tobi01001/android-guitar-notes-learner"
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/$REPOSITORY/releases/latest"
    private const val TIMEOUT_MS = 10_000

    fun currentVersion(context: Context): String =
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (e: Exception) {
            "0.0.0"
        }

    /** Returns the latest release, or throws on network/parse errors. */
    suspend fun fetchLatestRelease(): ReleaseInfo =
        withContext(Dispatchers.IO) {
            val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw java.io.IOException("HTTP ${connection.responseCode}")
                }
                parseRelease(connection.inputStream.bufferedReader().use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }

    internal fun parseRelease(json: String): ReleaseInfo {
        val obj = JSONObject(json)
        val tag = obj.getString("tag_name")
        val assets = obj.optJSONArray("assets")
        var apkUrl: String? = null
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.getString("browser_download_url")
                    break
                }
            }
        }
        return ReleaseInfo(
            version = tag.removePrefix("v"),
            downloadUrl = apkUrl ?: obj.getString("html_url"),
            releaseNotes = obj.optString("body"),
        )
    }

    /** True if [latest] is a higher version than [current] (numeric dotted comparison). */
    fun isNewer(
        latest: String,
        current: String,
    ): Boolean {
        fun parts(v: String) = v.removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val a = parts(latest)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
