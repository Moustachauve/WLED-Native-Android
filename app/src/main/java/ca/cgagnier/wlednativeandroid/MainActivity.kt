package ca.cgagnier.wlednativeandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ca.cgagnier.wlednativeandroid.ui.MainNavHost
import ca.cgagnier.wlednativeandroid.ui.theme.WLEDNativeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WLEDNativeTheme {
                MainNavHost()
            }
        }
    }
}