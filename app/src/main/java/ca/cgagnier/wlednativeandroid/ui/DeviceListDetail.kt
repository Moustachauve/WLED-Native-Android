package ca.cgagnier.wlednativeandroid.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DeviceListDetail(
    modifier: Modifier = Modifier,
    viewModel: DeviceListViewModel = hiltViewModel(),
) {
    val devices = viewModel.allDevicesFlow.collectAsState(initial = emptyList())
    val defaultScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val customScaffoldDirective = defaultScaffoldDirective.copy(
        horizontalPartitionSpacerSize = 0.dp,
    )
    val navigator =
        rememberListDetailPaneScaffoldNavigator<Any>(scaffoldDirective = customScaffoldDirective)
    var selectedDevice = navigator.currentDestination?.content as? Device

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
                        devices,
                        selectedDevice,
                        onItemClick = { device ->
                            selectedDevice = device
                            navigator.navigateTo(
                                pane = ListDetailPaneScaffoldRole.Detail,
                                content = device
                            )
                        },
                    )
                }
            }, detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.content?.let {
                        DeviceDetail(
                            it as Device,
                            canNavigateBack = navigator.canNavigateBack(),
                            navigateUp = {
                                //selectedDevice = null
                                navigator.navigateBack()
                            }
                        )
                    }
                }
            }, extraPane = {
                val content =
                    navigator.currentDestination?.content?.toString() ?: "Select an option"
                AnimatedPane {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = content)
                    }
                }
            }
        )
    }
}