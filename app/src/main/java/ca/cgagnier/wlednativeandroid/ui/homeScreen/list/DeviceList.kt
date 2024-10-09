package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd.DeviceAdd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeviceList(
    selectedDevice: Device?,
    isDiscovering: Boolean = false,
    onItemClick: (Device) -> Unit,
    onItemEdit: (Device) -> Unit,
    onRefresh: () -> Unit,
    openDrawer: () -> Unit,
    viewModel: DeviceListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val devices by viewModel.devices.collectAsState()

    val isKeyboardOpen by keyboardAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val listState = rememberLazyListState()
    val expandedFab by remember { derivedStateOf { !listState.canScrollBackward } }
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

    Scaffold(
        topBar = {
            DeviceListAppBar(
                isDiscovering = isDiscovering,
                openDrawer = openDrawer,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                expanded = expandedFab,
                text = {
                    Text(text = stringResource(R.string.add_a_device))
                },
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_a_device)
                    )
                },
                onClick = {
                    viewModel.showBottomSheet()
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,

        ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(innerPadding),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = refresh,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                itemsIndexed(devices, key = { _, device -> device.address }) { _, device ->
                    val swipeDismissState = rememberSwipeToDismissBoxState(
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

            if (uiState.showBottomSheet) {
                if (isKeyboardOpen) {
                    LaunchedEffect("keyboardOpen") {
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
                        deviceAdded = {
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.hideBottomSheet()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    ConfirmDeleteDialog(
        device = confirmDeleteDevice.value,
        onConfirm = {
            confirmDeleteDevice.value?.let {
                viewModel.deleteDevice(it)
                confirmDeleteDevice.value = null
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
    isDiscovering: Boolean = false,
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
        actions = {
            if (isDiscovering) {
                Column(
                    Modifier.padding(end = 16.dp),
                    horizontalAlignment = CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.padding(top = 6.dp),
                        painter = painterResource(R.drawable.baseline_wifi_find_24),
                        contentDescription = stringResource(R.string.discovering_devices),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(40.dp)
                            .padding(top = 6.dp)
                    )
                }
            }
        }
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


@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}