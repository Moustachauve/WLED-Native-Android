package ca.cgagnier.wlednativeandroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.cgagnier.wlednativeandroid.ui.homeScreen.DeviceListDetail
import ca.cgagnier.wlednativeandroid.ui.settingsScreen.Settings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController(),
    deviceMacAddress: StateFlow<String?>
) {
    val currentDeviceMacAddress by deviceMacAddress.collectAsStateWithLifecycle()
    NavHost(
        navController = navController,
        startDestination = DeviceListDetailScreen,
    ) {
        composable<DeviceListDetailScreen> {
            DeviceListDetail(
                openSettings = {
                    navController.navigate(SettingsScreen)
                },
                deviceMacAddress = currentDeviceMacAddress
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
