package ca.cgagnier.wlednativeandroid.ui.homeScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.homeScreen.detail.DeviceDetail
import ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd.DeviceAdd
import ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceEdit.DeviceEdit
import ca.cgagnier.wlednativeandroid.ui.homeScreen.list.DeviceList
import kotlinx.coroutines.launch

private const val TAG = "screen_DeviceListDetail"

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DeviceListDetail(
    modifier: Modifier = Modifier,
    openSettings: () -> Unit,
    viewModel: DeviceListDetailViewModel = hiltViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val defaultScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val customScaffoldDirective = defaultScaffoldDirective.copy(
        horizontalPartitionSpacerSize = 0.dp,
    )
    val navigator =
        rememberListDetailPaneScaffoldNavigator<Any>(scaffoldDirective = customScaffoldDirective)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val selectedDeviceAddress = navigator.currentDestination?.content as? String ?: ""
    val selectedDevice =
        viewModel.getDeviceByAddress(selectedDeviceAddress).collectAsStateWithLifecycle(null)

    val showHiddenDevices by viewModel.showHiddenDevices.collectAsStateWithLifecycle()
    val isWLEDCaptivePortal by viewModel.isWLEDCaptivePortal.collectAsStateWithLifecycle()
    val isAddDeviceBottomSheetVisible by viewModel.isAddDeviceBottomSheetVisible.collectAsStateWithLifecycle()
    val addDevice = {
        viewModel.showAddDeviceBottomSheet()
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.i(TAG, "== ON RESUME ==")
                viewModel.startDiscoveryServiceTimed()
                viewModel.startRefreshDevicesLoop()
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                Log.i(TAG, "== ON PAUSE ==")
                viewModel.stopRefreshDevicesLoop()
                viewModel.stopDiscoveryService()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    showHiddenDevices = showHiddenDevices,
                    addDevice = {
                        coroutineScope.launch {
                            addDevice()
                            drawerState.close()
                        }
                    },
                    toggleShowHiddenDevices = {
                        coroutineScope.launch {
                            viewModel.toggleShowHiddenDevices()
                            drawerState.close()
                        }
                    },
                    openSettings = {
                        coroutineScope.launch {
                            openSettings()
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) { innerPadding ->
            NavigableListDetailPaneScaffold(
                modifier = modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                navigator = navigator,
                defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
                listPane = {
                    AnimatedPane {
                        DeviceList(
                            selectedDevice.value,
                            isWLEDCaptivePortal = isWLEDCaptivePortal,
                            onItemClick = navigateToDeviceDetail,
                            onAddDevice = addDevice,
                            onShowHiddenDevices = {
                                viewModel.toggleShowHiddenDevices()
                            },
                            onRefresh = {
                                viewModel.refreshDevices(silent = false)
                                viewModel.startDiscoveryServiceTimed()
                            },
                            onItemEdit = {
                                navigateToDeviceDetail(it)
                                navigateToDeviceEdit(it)
                            },
                            onOpenDrawer = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        )
                    }
                }, detailPane = {
                    AnimatedPane {
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
                        } ?: SelectDeviceView()
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


    if (isAddDeviceBottomSheetVisible) {
        AddDeviceBottomSheet(sheetState, onDismissRequest = {
            viewModel.hideAddDeviceBottomSheet()
        })
    }
}

@Composable
private fun DrawerContent(
    showHiddenDevices: Boolean,
    addDevice: () -> Unit,
    toggleShowHiddenDevices: () -> Unit,
    openSettings: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.Center) {
        Image(
            painter = painterResource(id = R.drawable.illumidel_logo),
            contentDescription = stringResource(R.string.app_logo)
        )
    }
    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.add_a_device)) },
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_a_device)
            )
        },
        selected = false,
        onClick = addDevice,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    ToggleHiddenDeviceButton(showHiddenDevices, toggleShowHiddenDevices)
    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.settings)) },
        icon = {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        },
        selected = false,
        onClick = openSettings,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    HorizontalDivider(modifier = Modifier.padding(12.dp))

    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.visit_website)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_launch_24),
                contentDescription = stringResource(R.string.visit_website),
            )
        },
        selected = false,
        onClick = {
            uriHandler.openUri("https://illumidel.com/")
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.help)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_help_24),
                contentDescription = stringResource(R.string.help)
            )
        },
        selected = false,
        onClick = {
            uriHandler.openUri("https://illumidel.com/videos")
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
private fun ToggleHiddenDeviceButton(
    showHiddenDevices: Boolean,
    toggleShowHiddenDevices: () -> Unit
) {
    val hiddenDeviceText = stringResource(
        if (showHiddenDevices) R.string.hide_hidden_devices
        else R.string.show_hidden_devices
    )
    val hiddenDeviceIcon = painterResource(
        if (showHiddenDevices) R.drawable.ic_baseline_visibility_off_24
        else R.drawable.baseline_visibility_24
    )
    NavigationDrawerItem(
        label = { Text(text = hiddenDeviceText) },
        icon = {
            Icon(
                painter = hiddenDeviceIcon,
                contentDescription = stringResource(R.string.show_hidden_devices)
            )
        },
        selected = false,
        onClick = toggleShowHiddenDevices,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun SelectDeviceView() {
    Card(
        modifier = Modifier.padding(top = TopAppBarDefaults.MediumAppBarCollapsedHeight),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.illumidel_logo),
                contentDescription = stringResource(R.string.app_logo)
            )
            Text(
                stringResource(R.string.select_a_device_from_the_list)
            )
        }
    }
}

@Composable
private fun AddDeviceBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        DeviceAdd(
            sheetState = sheetState,
            deviceAdded = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            },
        )
    }
}