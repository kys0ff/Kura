package off.kys.kura.features.main.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import cafe.adriel.voyager.navigator.Navigator
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.features.main.presentation.screen.MainScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val kuraPreferences: KuraPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode = kuraPreferences.themeMode
            val dynamicColor = kuraPreferences.dynamicColorEnabled

            KuraTheme(
                darkTheme = when (themeMode) {
                    "LIGHT" -> false
                    "DARK" -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = dynamicColor
            ) {
                Navigator(screen = MainScreen())
            }
        }
    }
}