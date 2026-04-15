package off.kys.kura.core.common

enum class LockStyle {
    PULSE,
    FROZEN,
    WALLPAPER;

    operator fun invoke(): String = name

    companion object {
        fun fromString(value: String): LockStyle = entries.find { it.name == value } ?: PULSE
    }
}