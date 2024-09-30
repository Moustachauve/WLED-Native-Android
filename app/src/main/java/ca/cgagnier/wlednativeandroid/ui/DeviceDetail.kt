package ca.cgagnier.wlednativeandroid.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.components.DeviceWebView
import ca.cgagnier.wlednativeandroid.ui.components.LoadingState
import ca.cgagnier.wlednativeandroid.ui.components.WebViewState
import ca.cgagnier.wlednativeandroid.ui.components.rememberSaveableWebViewState
import ca.cgagnier.wlednativeandroid.ui.components.rememberWebViewNavigator

private const val TAG = "ui.DeviceDetail"

@Composable
fun DeviceDetailAppBar(
    device: Device,
    canNavigateBack: Boolean,
    webViewState: WebViewState,
    navigateUp: () -> Unit,
    refreshPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = device.name)
                    Text(text = device.address)
                }
                if (webViewState.loadingState is LoadingState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .height(22.dp)
                            .width(22.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
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
            IconButton(onClick = refreshPage) {
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
    val webViewState = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()
    Scaffold(
        topBar = {
            DeviceDetailAppBar(
                device = device,
                canNavigateBack = canNavigateBack,
                webViewState = webViewState,
                navigateUp = navigateUp,
                refreshPage = {
                    navigator.reload()
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LaunchedEffect(navigator) {
                val bundle = webViewState.viewState
                if (bundle == null) {
                    Log.i(TAG, "Loading device for first time")
                    // This is the first time load, so load the home page.
                    //navigator.loadUrl(device.getDeviceUrl())
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