package ca.cgagnier.wlednativeandroid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import ca.cgagnier.wlednativeandroid.service.websocket.DeviceWithState

@Composable
@ReadOnlyComposable
fun deviceName(device: DeviceWithState): String {
    val name = device.device.customName ?: device.device.originalName
    if (name != null && name.isNotBlank()) {
        return name
    }
    return stringResource(R.string.default_device_name)
}