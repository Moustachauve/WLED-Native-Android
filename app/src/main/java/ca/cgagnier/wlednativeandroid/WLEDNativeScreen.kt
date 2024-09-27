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
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import kotlinx.serialization.Serializable

@Composable
fun WLEDNativeApp(
    appContainer: AppContainer,
    viewModel: DeviceListViewModel = viewModel(
        factory = DeviceListViewModelFactory(
            appContainer.deviceRepository, appContainer.userPreferencesRepository
        )
    ),
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

@Serializable
object DeviceAdd