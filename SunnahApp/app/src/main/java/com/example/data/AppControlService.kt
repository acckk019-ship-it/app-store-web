package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Data model representing the remote control.json from the App Store website.
 * This is fetched on every app startup to allow remote management of the app.
 */
data class AppControl(
    /** Latest version string available on the store (e.g. "22") */
    val latestVersion: String = "",
    val latestVersionName: String = "",
    /** Message to show when a newer version is available */
    val updateMessage: String = "",
    /** URL to open when user taps "Update" */
    val updateUrl: String = "",
    /** If true, display a full-screen force-close dialog and disable the app */
    val forceClose: Boolean = false,
    val forceCloseMessage: String = "",
    /** If true, show a persistent "Under Development" banner */
    val underDevelopment: Boolean = false,
    val underDevelopmentMessage: String = "",
    val lastUpdated: String = ""
)

/**
 * Fetches and parses the control.json file from GitHub Pages.
 * This is the live remote-control channel between the web admin and the app.
 */
object AppControlService {

    // The GitHub Pages URL of the control.json — update this if the repo changes
    private const val CONTROL_URL =
        "https://acckk019-ship-it.github.io/app-store-web/control.json"

    // Current version of this app build (matches the version in apps.json)
    const val CURRENT_VERSION = "22"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches the remote control.json and returns an [AppControl] object.
     * Returns a default (safe) AppControl on any network or parse failure.
     */
    suspend fun fetchControl(): AppControl = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(CONTROL_URL)
                .cacheControl(
                    okhttp3.CacheControl.Builder()
                        .noCache()
                        .build()
                )
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext AppControl()

            val body = response.body?.string() ?: return@withContext AppControl()
            val cleanBody = if (body.startsWith("\uFEFF")) body.substring(1) else body
            val json = JSONObject(cleanBody)

            AppControl(
                latestVersion      = json.optString("latestVersion", ""),
                latestVersionName  = json.optString("latestVersionName", ""),
                updateMessage      = json.optString("updateMessage", ""),
                updateUrl          = json.optString("updateUrl", ""),
                forceClose         = json.optBoolean("forceClose", false),
                forceCloseMessage  = json.optString("forceCloseMessage", ""),
                underDevelopment   = json.optBoolean("underDevelopment", false),
                underDevelopmentMessage = json.optString("underDevelopmentMessage", ""),
                lastUpdated        = json.optString("lastUpdated", "")
            )
        } catch (e: Exception) {
            // Network failure or JSON error — return safe defaults (app runs normally)
            AppControl()
        }
    }

    /**
     * Returns true if the remote latestVersion is higher than the current build version.
     * Compares version strings as integers; falls back to string comparison.
     */
    fun isUpdateAvailable(control: AppControl): Boolean {
        if (control.latestVersion.isBlank()) return false
        return try {
            val remote = control.latestVersion.trim().toInt()
            val current = CURRENT_VERSION.trim().toInt()
            remote > current
        } catch (e: NumberFormatException) {
            control.latestVersion.trim() != CURRENT_VERSION.trim() &&
                control.latestVersion.trim() > CURRENT_VERSION.trim()
        }
    }
}
