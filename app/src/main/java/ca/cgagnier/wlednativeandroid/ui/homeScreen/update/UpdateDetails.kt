package ca.cgagnier.wlednativeandroid.ui.homeScreen.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.ui.components.DeviceName
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography

@Composable
fun UpdateDetails(
    device: Device,
    onDismiss: () -> Unit,
    updateDetailsViewModel: UpdateDetailsViewModel = hiltViewModel()
) {
    val state by updateDetailsViewModel.state.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLargeScreen = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    LaunchedEffect(device) {
        updateDetailsViewModel.setDevice(device)
    }
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = isLargeScreen),
        onDismissRequest = onDismiss,
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column {
                TopHeader(device)
                ReleaseNotes(
                    modifier = Modifier.weight(1f),
                    version = state.version
                )
                BottomNavigationBar(onDismiss)
            }
        }
    }
}

@Composable
private fun TopHeader(device: Device) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                stringResource(R.string.update_available),
                style = MaterialTheme.typography.titleLarge
            )
            Row {
                Text(
                    DeviceName(device) + " - " + device.address,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ReleaseNotes(
    modifier: Modifier = Modifier,
    version: Version? = null
) {
    if (version != null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Markdown(
                version.description.trimIndent(),
                typography = markdownTypography(
                    h1 = MaterialTheme.typography.headlineLarge,
                    h2 = MaterialTheme.typography.headlineMedium,
                    h3 = MaterialTheme.typography.headlineSmall,
                )
            )
        }
    } else {
        CircularProgressIndicator()
    }
}

@Composable
private fun BottomNavigationBar(onDismiss: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_download_24),
                    contentDescription = stringResource(R.string.install)
                )
            },
            label = {
                Text(stringResource(R.string.install))
            },
            selected = true,
            onClick = onDismiss
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_update_24),
                    contentDescription = stringResource(R.string.later)
                )
            },
            label = {
                Text(stringResource(R.string.later))
            },
            selected = false,
            onClick = onDismiss
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_update_disabled_24),
                    contentDescription = stringResource(R.string.skip_this_version)
                )
            },
            label = {
                Text(stringResource(R.string.skip_this_version))
            },
            selected = false,
            onClick = onDismiss
        )

    }
}