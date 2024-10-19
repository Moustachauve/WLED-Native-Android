package ca.cgagnier.wlednativeandroid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device

@Composable
@ReadOnlyComposable
fun DeviceName(device: Device): String {
    if (device.name.isNotBlank()) {
        return device.name
    }
    return stringResource(R.string.default_device_name)
}