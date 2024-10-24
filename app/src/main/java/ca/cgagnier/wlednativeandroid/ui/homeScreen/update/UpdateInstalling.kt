package ca.cgagnier.wlednativeandroid.ui.homeScreen.update

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.ui.components.DeviceName
import ca.cgagnier.wlednativeandroid.ui.theme.WLEDNativeTheme

@Composable
fun UpdateInstallingDialog(
    device: Device,
    version: VersionWithAssets,
    onDismiss: () -> Unit,
    viewModel: UpdateInstallingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(device, version) {
        viewModel.startUpdate(device, version)
    }

    UpdateInstallingDialog(
        state = state,
        device = device,
        version = version,
        onDismiss = onDismiss
    )
}

@Composable
fun UpdateInstallingDialog(
    state: UpdateInstallingState,
    device: Device,
    version: VersionWithAssets,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(
                stringResource(
                    R.string.updating,
                    DeviceName(device)
                )
            )
        },
        text = {
            UpdateDialogContent(
                version = version,
                state = state
            )
        },
        onDismissRequest = {
            if (state.canDismiss) {
                onDismiss()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
                enabled = state.canDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        dismissButton = {
            if (state.step is UpdateInstallingStep.Error) {
                val text = if (state.step.showError) R.string.hide_error else R.string.show_error
                TextButton(
                    onClick = {
                        // TODO
                    },
                ) {
                    Text(stringResource(text))
                }
            }
        },
        icon = {},
    )
}

@Composable
fun UpdateDialogContent(
    version: VersionWithAssets,
    state: UpdateInstallingState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UpdateInstallingStatus(
            modifier = Modifier
                .height(48.dp)
                .width(48.dp),
            step = state.step
        )
        Text(
            updateInstallingStatusMessage(step = state.step),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(version.version.tagName)
        if (!state.canDismiss) {
            Text(
                stringResource(R.string.please_do_not_close_the_app_or_turn_off_the_device),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        if (state.step is UpdateInstallingStep.Error && state.step.showError) {
            ErrorMessageCard(state.step)
        }
    }
}

@Composable
private fun UpdateInstallingStatus(
    modifier: Modifier = Modifier,
    step: UpdateInstallingStep
) {
    when (step) {
        is UpdateInstallingStep.Starting -> CircularProgressIndicator(modifier)
        is UpdateInstallingStep.Downloading -> CircularProgressIndicator(modifier)
        is UpdateInstallingStep.Installing -> CircularProgressIndicator(modifier)
        is UpdateInstallingStep.Error -> Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.baseline_error_outline_24),
            contentDescription = stringResource(R.string.update_failed),
            tint = MaterialTheme.colorScheme.error
        )
        is UpdateInstallingStep.Done -> Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_twotone_check_circle_outline_24),
            contentDescription = stringResource(R.string.update_completed),
            tint = Color(0xFF00b300)
        )
    }
}

@Composable
private fun updateInstallingStatusMessage(
    step: UpdateInstallingStep
): String {
    return when (step) {
        is UpdateInstallingStep.Starting -> stringResource(R.string.starting_up)
        is UpdateInstallingStep.Downloading -> stringResource(R.string.downloading_version)
        is UpdateInstallingStep.Installing -> stringResource(R.string.installing_update)
        is UpdateInstallingStep.Error -> stringResource(R.string.update_failed)
        is UpdateInstallingStep.Done -> stringResource(R.string.update_completed)
    }
}

@Composable
private fun ErrorMessageCard(step: UpdateInstallingStep.Error) {
    Card(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .width(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            step.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .width(IntrinsicSize.Min),
        )
    }
}

class SampleStateStepProvider : PreviewParameterProvider<UpdateInstallingState> {
    override val values = sequenceOf(
        UpdateInstallingState(
            step = UpdateInstallingStep.Starting,
            canDismiss = true
        ),
        UpdateInstallingState(
            step = UpdateInstallingStep.Downloading,
            canDismiss = true
        ),
        UpdateInstallingState(
            step = UpdateInstallingStep.Installing,
            canDismiss = false
        ),
        UpdateInstallingState(
            step = UpdateInstallingStep.Error("Something went wrong", showError = false),
            canDismiss = true
        ),
        UpdateInstallingState(
            step = UpdateInstallingStep.Error(
                "Something went wrong. This is the error message. It should be longer than one line preferably.",
                showError = true
            ), canDismiss = true
        ),
        UpdateInstallingState(
            step = UpdateInstallingStep.Done,
            canDismiss = true
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateInstallingDialogStepStartingPreview(
    @PreviewParameter(SampleStateStepProvider::class) state: UpdateInstallingState
) {
    WLEDNativeTheme(darkTheme = isSystemInDarkTheme()) {
        UpdateInstallingDialog(
            state = state,
            device = Device.getPreviewDevice(),
            version = VersionWithAssets(
                version = Version.getPreviewVersion(),
                assets = listOf()
            ),
            onDismiss = {},
        )
    }
}