package off.kys.kura.core.data.model

/**
 * Represents an app's information.
 *
 * @property name The name of the app.
 * @property packageName The package name of the app.
 */
data class AppInfo(
    val name: String,
    val packageName: String
)