package ca.cgagnier.wlednativeandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ca.cgagnier.wlednativeandroid.ui.theme.WLEDNativeTheme


class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy {
        (application as DevicesApplication).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WLEDNativeTheme {
                WLEDNativeApp(appContainer)
            }
        }
    }
}