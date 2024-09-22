package ca.cgagnier.wlednativeandroid.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.components.DeviceWebView
import ca.cgagnier.wlednativeandroid.ui.components.rememberSaveableWebViewState
import ca.cgagnier.wlednativeandroid.ui.components.rememberWebViewNavigator

@Composable
fun DeviceDetailAppBar(
    device: Device,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(text = device.name)
                Text(text = device.address)
            }
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
        actions = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.refresh_page)
                )
            }
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit_device)
                )
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DeviceDetail(
    device: Device,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            DeviceDetailAppBar(
                canNavigateBack = canNavigateBack,
                navigateUp = navigateUp,
                device = device
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val webViewState = rememberSaveableWebViewState()
            val navigator = rememberWebViewNavigator()

            LaunchedEffect(navigator) {
                val bundle = webViewState.viewState
                if (bundle == null) {
                    // This is the first time load, so load the home page.
                    navigator.loadUrl(device.getDeviceUrl())
                }
            }
            DeviceWebView(
                device,
                state = webViewState,
                navigator = navigator
            )
            /*
            Row {
                AssistChip(onClick = {
                    //navigator.navigateTo(
                    //    pane = ListDetailPaneScaffoldRole.Extra, content = "Option 1"
                    //)
                }, label = {
                    Text(text = "Option 1")
                })
            }*/
        }
    }
}