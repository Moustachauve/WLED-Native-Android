package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd.DeviceAdd
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
    onRefresh: () -> Unit,
    openDrawer: () -> Unit,
    viewModel: DeviceListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()

    val isKeyboardOpen by keyboardAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val listState = rememberLazyListState()
    val isFabExpanded by remember { derivedStateOf { !listState.canScrollBackward } }
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val confirmDeleteDevice: MutableState<Device?> = remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()

    val refresh: () -> Unit = {
        isRefreshing = true
        onRefresh()
        coroutineScope.launch {
            delay(4000)
            isRefreshing = false
        }
    }
    val addDevice = {
        viewModel.showBottomSheet()
    }

    Scaffold(
        topBar = {
            DeviceListAppBar(
                openDrawer = openDrawer,
            )
        },
        floatingActionButton = {
            if (devices.isNotEmpty()) {
                FloatingActionButton(
                    isFabExpanded = isFabExpanded,
                    addDevice = addDevice
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,

        ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = refresh,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
                    .clip(shape = MaterialTheme.shapes.large),
            ) {
                if (devices.isEmpty()) {
                    item {
                        NoDevicesItem(
                            modifier = Modifier.fillParentMaxSize(), addDevice = addDevice
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
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    Log.d(TAG, "asking confirm delete device")
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
                                        Log.d(TAG, "delete swipe reset")
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

    if (uiState.showBottomSheet) {
        AddDeviceBottomSheet(isKeyboardOpen, sheetState, viewModel)
    }

    ConfirmDeleteDialog(
        device = confirmDeleteDevice.value,
        onConfirm = {
            Log.d(TAG, "Confirm deleting device")
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
fun FloatingActionButton(
    isFabExpanded: Boolean,
    addDevice: () -> Unit,
) {
    ExtendedFloatingActionButton(
        expanded = isFabExpanded,
        text = {
            Text(text = stringResource(R.string.add_a_device))
        },
        icon = {
            Icon(
                Icons.Filled.Add, contentDescription = stringResource(R.string.add_a_device)
            )
        },
        onClick = addDevice
    )
}

@Composable
private fun AddDeviceBottomSheet(
    isKeyboardOpen: Boolean,
    sheetState: SheetState,
    viewModel: DeviceListViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen) {
            delay(300)
            sheetState.expand()
        }
    }
    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        onDismissRequest = {
            viewModel.hideBottomSheet()
        },
    ) {
        DeviceAdd(
            sheetState = sheetState,
            deviceAdded = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        viewModel.hideBottomSheet()
                    }
                }
            },
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    device: Device? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    device?.let {
        Log.d(TAG, "Composing confirm delete dialog, device: ${device.name}")
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


@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}