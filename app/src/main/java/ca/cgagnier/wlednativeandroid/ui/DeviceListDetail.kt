package ca.cgagnier.wlednativeandroid.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.cgagnier.wlednativeandroid.model.Device

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DeviceListDetail(devices: State<List<Device>>, modifier: Modifier = Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
        listPane = {
            AnimatedPane {
                DeviceList(
                    devices,
                    onItemClick = { device ->
                        navigator.navigateTo(
                            pane = ListDetailPaneScaffoldRole.Detail,
                            content = device
                        )
                    }
                )
            }
        }, detailPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let {
                    DeviceDetail(
                        it as Device,
                        canNavigateBack = navigator.canNavigateBack(),
                        navigateUp = { navigator.navigateBack() }
                    )
                }
            }
        }, extraPane = {
            val content = navigator.currentDestination?.content?.toString() ?: "Select an option"
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