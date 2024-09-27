package ca.cgagnier.wlednativeandroid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.cgagnier.wlednativeandroid.ui.DeviceListDetail
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import kotlinx.serialization.Serializable

@Composable
fun WLEDNativeApp(
    viewModel: DeviceListViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = DeviceListDetail,
    ) {
        composable<DeviceListDetail> {
            val devices = viewModel.allDevicesFlow.collectAsState(initial = emptyList())
            DeviceListDetail(
                devices = devices,
            )
        }
    }
}

@Serializable
object DeviceListDetail
