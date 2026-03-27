package off.kys.kura.features.main.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Badge {
    @SerialName("Crucial")
    CRUCIAL,

    @SerialName("Recommended")
    RECOMMENDED,

    @SerialName("HasInAppLock")
    HAS_IN_APP_LOCK
}