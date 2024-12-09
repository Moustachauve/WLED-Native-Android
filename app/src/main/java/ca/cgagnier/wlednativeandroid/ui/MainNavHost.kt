package ca.cgagnier.wlednativeandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.cgagnier.wlednativeandroid.ui.homeScreen.DeviceListDetail
import ca.cgagnier.wlednativeandroid.ui.settingsScreen.Settings
import kotlinx.serialization.Serializable

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController()
) {
    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surface,
                    Color(0xFF003765),
                    MaterialTheme.colorScheme.surface,
                )
            )
        ).fillMaxSize()
    )
    NavHost(
        navController = navController,
        startDestination = DeviceListDetailScreen,
    ) {
        composable<DeviceListDetailScreen> {
            DeviceListDetail(
                openSettings = {
                    navController.navigate(SettingsScreen)
                }
            )
        }
        composable<SettingsScreen> {
            Settings(
                navigateUp = {
                    navController.navigateUp()
                }
            )
        }
    }
}

@Serializable
object DeviceListDetailScreen

@Serializable
object SettingsScreen
