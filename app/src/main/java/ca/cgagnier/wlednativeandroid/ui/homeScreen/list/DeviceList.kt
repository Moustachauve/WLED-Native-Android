package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.theme.DeviceTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "screen_DeviceList"

@Composable
fun DeviceList(
    selectedDevice: Device?,
    isWLEDCaptivePortal: Boolean = false,
    onItemClick: (Device) -> Unit,
    onItemEdit: (Device) -> Unit,
    onAddDevice: () -> Unit,
    onShowHiddenDevices: () -> Unit,
    onRefresh: () -> Unit,
    openDrawer: () -> Unit,
    viewModel: DeviceListViewModel = hiltViewModel(),
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val shouldShowDevicesAreHidden by viewModel.shouldShowDevicesAreHidden.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val confirmDeleteDevice: MutableState<Device?> = remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()

    val refresh: () -> Unit = {
        isRefreshing = true
        onRefresh()
        coroutineScope.launch {
            delay(1800)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            DeviceListAppBar(
                openDrawer = openDrawer,
            )
        },
        ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = refresh,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
                    .clip(shape = MaterialTheme.shapes.large),
            ) {
                if (devices.isEmpty()) {
                    item {
                        NoDevicesItem(
                            modifier = Modifier.fillParentMaxSize(),
                            shouldShowHiddenDevices = shouldShowDevicesAreHidden,
                            onAddDevice = onAddDevice,
                            onShowHiddenDevices = onShowHiddenDevices
                        )
                    }
                } else {
                    if (isWLEDCaptivePortal) {
                        item {
                            val device = Device.getDefaultAPDevice()
                            DeviceAPListItem(
                                isSelected = device.address == selectedDevice?.address,
                                onClick = { onItemClick(device) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                    itemsIndexed(devices, key = { _, device -> device.address }) { _, device ->
                        val swipeDismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { distance -> distance * 0.3f },
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    confirmDeleteDevice.value = device
                                    return@rememberSwipeToDismissBoxState true
                                } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                                    onItemEdit(device)
                                    return@rememberSwipeToDismissBoxState false
                                }
                                true
                            },
                        )
                        DeviceListItem(
                            device = device,
                            isSelected = device.address == selectedDevice?.address,
                            onClick = { onItemClick(device) },
                            swipeToDismissBoxState = swipeDismissState,
                            onPowerSwitchToggle = { isOn ->
                                viewModel.toggleDevicePower(device, isOn)
                            },
                            onBrightnessChanged = { brightness ->
                                viewModel.setDeviceBrightness(device, brightness)
                            },
                            modifier = Modifier.animateItem()
                        )
                        when (confirmDeleteDevice.value) {
                            null -> {
                                if (swipeDismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                    LaunchedEffect(Unit) {
                                        swipeDismissState.reset()
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.padding(42.dp))
                    }
                }
            }
        }
    }

    ConfirmDeleteDialog(
        device = confirmDeleteDevice.value,
        onConfirm = {
            confirmDeleteDevice.value?.let {
                coroutineScope.launch {
                    viewModel.deleteDevice(it)
                    // Without this delay, the delete confirmation dialog would sometime show up
                    // twice, mostly on tablets. This ensures that the dialog is hidden after the
                    // device item has been removed from the list, otherwise a badly timed
                    // recomposition would cause the double dialog.
                    delay(1)
                    confirmDeleteDevice.value = null
                }
            }
        },
        onDismiss = {
            confirmDeleteDevice.value = null
        }
    )
}

@Composable
fun DeviceListAppBar(
    modifier: Modifier = Modifier,
    openDrawer: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Image(
                painter = painterResource(id = R.drawable.wled_logo_akemi),
                contentDescription = stringResource(R.string.app_logo)
            )
        },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.description_menu_button)
                )
            }
        },
    )
}

@Composable
fun ConfirmDeleteDialog(
    device: Device? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    device?.let {
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.deleting_device))
            },
            text = {
                DeviceTheme(device) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        DeviceInfoTwoRows(
                            modifier = Modifier.padding(16.dp),
                            device = device
                        )
                    }
                }
            },
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
