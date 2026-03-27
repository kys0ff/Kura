package off.kys.kura.features.main.data

import kotlinx.serialization.Serializable
import off.kys.kura.features.main.data.util.BadgeSerializer

@Serializable(with = BadgeSerializer::class)
enum class Badge(val index: Int) {
    RECOMMENDED(0),
    CRUCIAL(1),
    HAS_IN_APP_LOCK(2),
    NOT_RECOMMENDED(3);

    companion object {
        // Helper to find the badge by index efficiently
        fun fromInt(index: Int) = entries.find { it.index == index } ?: RECOMMENDED
    }
}