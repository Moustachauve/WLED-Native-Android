package ca.cgagnier.wlednativeandroid.ui.homeScreen

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.homeScreen.detail.DeviceDetail
import ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceEdit.DeviceEdit
import ca.cgagnier.wlednativeandroid.ui.homeScreen.list.DeviceList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DeviceListDetail(
    modifier: Modifier = Modifier,
    viewModel: DeviceListDetailViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val defaultScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val customScaffoldDirective = defaultScaffoldDirective.copy(
        horizontalPartitionSpacerSize = 0.dp,
    )
    val navigator =
        rememberListDetailPaneScaffoldNavigator<Any>(scaffoldDirective = customScaffoldDirective)

    val selectedDeviceAddress = navigator.currentDestination?.content as? String ?: ""
    val selectedDevice =
        viewModel.getDeviceByAddress(selectedDeviceAddress).collectAsState(null)

    val startDiscovery = {
        coroutineScope.launch {
            viewModel.startDiscoveryService()
            delay(15000)
            viewModel.stopDiscoveryService()
        }
    }

    LaunchedEffect(viewModel.isPolling) {
        viewModel.startRefreshDevicesLoop()
    }
    LaunchedEffect("onStart-startDiscovery") {
        startDiscovery()
    }

    val navigateToDeviceDetail = { device: Device ->
        navigator.navigateTo(
            pane = ListDetailPaneScaffoldRole.Detail,
            content = device.address
        )
    }
    val navigateToDeviceEdit = { device: Device ->
        navigator.navigateTo(
            pane = ListDetailPaneScaffoldRole.Extra,
            content = device.address
        )
    }

    Scaffold { innerPadding ->
        NavigableListDetailPaneScaffold(
            modifier = modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            navigator = navigator,
            defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
            listPane = {
                AnimatedPane {
                    DeviceList(
                        selectedDevice.value,
                        onItemClick = navigateToDeviceDetail,
                        onRefresh = {
                            viewModel.refreshDevices(silent = false)
                            startDiscovery()
                        },
                        onItemEdit = {
                            navigateToDeviceDetail(it)
                            navigateToDeviceEdit(it)
                        },
                        isDiscovering = viewModel.isDiscovering
                    )
                }
            }, detailPane = {
                AnimatedPane {
                    // selectedDevice est static, pas dynamic de la database... Utiliser ID à la place
                    selectedDevice.value?.let { device ->
                        DeviceDetail(
                            device = device,
                            onItemEdit = {
                                navigateToDeviceEdit(device)
                            },
                            canNavigateBack = navigator.canNavigateBack(),
                            navigateUp = {
                                navigator.navigateBack()
                            }
                        )
                    }
                }
            }, extraPane = {
                AnimatedPane {
                    selectedDevice.value?.let { device ->
                        DeviceEdit(
                            device = device,
                            canNavigateBack = navigator.canNavigateBack(),
                            navigateUp = {
                                navigator.navigateBack()
                            }
                        )
                    }
                }
            }
        )
    }
}