package off.kys.kura.core.common

enum class LockAppearance {
    PULSE,
    FROZEN,
    WALLPAPER;

    operator fun invoke(): String = name

    companion object {
        fun fromString(value: String): LockAppearance = entries.find { it.name == value } ?: PULSE
    }
}