package ca.cgagnier.wlednativeandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.cgagnier.wlednativeandroid.ui.DeviceListDetail
import ca.cgagnier.wlednativeandroid.ui.theme.WLEDNativeTheme
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory


class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy {
        (application as DevicesApplication).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WLEDNativeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val deviceListViewModel: DeviceListViewModel = viewModel(
                        factory = DeviceListViewModelFactory(
                            appContainer.deviceRepository, appContainer.userPreferencesRepository
                        )
                    )
                    val devices =
                        deviceListViewModel.allDevicesFlow.collectAsState(initial = emptyList())
                    DeviceListDetail(devices, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
