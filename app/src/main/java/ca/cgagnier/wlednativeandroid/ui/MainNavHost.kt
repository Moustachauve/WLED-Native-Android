package ca.cgagnier.wlednativeandroid.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.cgagnier.wlednativeandroid.ui.homeScreen.DeviceListDetail
import kotlinx.serialization.Serializable

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = DeviceListDetail,
    ) {
        composable<DeviceListDetail> {
            DeviceListDetail()
        }
    }
}

@Serializable
object DeviceListDetail