package ca.cgagnier.wlednativeandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.components.DeviceListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeviceListAppBar(
    canNavigateBack: Boolean, navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.wled_logo_akemi),
                contentDescription = stringResource(R.string.app_logo)
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.description_back_button)
                    )
                }
            }
        },
    )
}

@Composable
fun DeviceList(
    devices: State<List<Device>>,
    selectedDevice: Device?,
    onItemClick: (Device) -> Unit,
) {
    val isKeyboardOpen by keyboardAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val expandedFab by remember { derivedStateOf { !listState.canScrollBackward } }

    Scaffold(
        topBar = {
            DeviceListAppBar(
                canNavigateBack = false,
                navigateUp = { /* TODO: implement back navigation */ },
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
                        Icons.Filled.Add, contentDescription = stringResource(R.string.add_a_device)
                    )
                },
                onClick = {
                    showBottomSheet = true
                },
            )
        },
        floatingActionButtonPosition = FabPosition.End,

        ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            itemsIndexed(devices.value) { _, device ->
                DeviceListItem(device,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .background(
                            if (device == selectedDevice) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = MaterialTheme.shapes.large
                        )
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            onItemClick(device)
                        })
            }
            item {
                Spacer(Modifier.padding(42.dp))
            }
        }

        if (showBottomSheet) {
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
                    showBottomSheet = false
                },
            ) {
                DeviceAdd(
                    deviceAdded = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }

        if (showBottomSheet) {
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
                    showBottomSheet = false
                },
            ) {
                DeviceAdd(
                    deviceAdded = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }

        if (showBottomSheet) {
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
                    showBottomSheet = false
                },
            ) {
                DeviceAdd(
                    deviceAdded = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}