package off.kys.kura.features.main.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.features.main.presentation.screen.MainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuraTheme {
                Navigator(screen = MainScreen())
            }
        }
    }
}