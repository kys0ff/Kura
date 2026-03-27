package off.kys.kura.features.main.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

private const val BADGES_JSON_URL = "https://raw.githubusercontent.com/kys0ff/Kura-Badges/refs/heads/main/badges.json"

class BadgeLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false 
    }

    // Using a thread-safe volatile map for the cache
    @Volatile
    private var badgeCache: Map<String, List<String>> = emptyMap()

    @OptIn(ExperimentalSerializationApi::class)
    fun loadJson(onComplete: (Boolean) -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(BADGES_JSON_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    doInput = true
                    connectTimeout = 10000
                    readTimeout = 15000
                }

                connection.inputStream.use { stream ->
                    badgeCache = json.decodeFromStream<Map<String, List<String>>>(stream)
                }
                
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun getBadges(packageName: String): List<String> = badgeCache[packageName] ?: emptyList()
}