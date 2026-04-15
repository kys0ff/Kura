package off.kys.kura.core.common

/**
 * Represents the visual style or effect applied to the lock interface.
 */
enum class LockAppearance {
    /**
     * Represents a pulsing animation style for the lock screen appearance.
     */
    PULSE,
    /** Represents a static, non-animated appearance for the lock. */
    FROZEN,
    /**
     * Uses the system wallpaper as the lock screen appearance.
     */
    WALLPAPER;

    /**
     * Returns the name of this [LockAppearance] constant.
     *
     * @return the name of the appearance as a [String].
     */
    operator fun invoke(): String = name

    companion object {
        /**
         * Returns the [LockAppearance] matching the given [value] name,
         * or [PULSE] as a fallback if no match is found.
         *
         * @param value The string representation of the appearance.
         * @return The corresponding [LockAppearance] or [PULSE].
         */
        fun fromString(value: String): LockAppearance = entries.find { it.name == value } ?: PULSE
    }
}