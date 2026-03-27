package off.kys.kura.features.main.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import off.kys.kura.features.main.data.Badge // Import your Enum
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

private const val BADGES_JSON_URL =
    "https://raw.githubusercontent.com/kys0ff/Kura-Badges/refs/heads/main/badges.json"

class BadgeLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Volatile
    private var badgeCache: Map<String, List<Badge>> = emptyMap()

    @OptIn(ExperimentalSerializationApi::class)
    fun loadJson(onComplete: (Boolean) -> Unit = {}) {
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

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { stream ->
                        // decoding directly to List<Badge>
                        badgeCache = json.decodeFromStream<Map<String, List<Badge>>>(stream)
                    }
                    onComplete(true)
                } else {
                    onComplete(false)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun getBadges(packageName: String): List<Badge> = badgeCache[packageName] ?: emptyList()
}