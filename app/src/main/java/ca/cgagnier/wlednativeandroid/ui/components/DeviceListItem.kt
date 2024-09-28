package ca.cgagnier.wlednativeandroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device

@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    device: Device,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    var sliderPosition by remember { mutableFloatStateOf(device.brightness.toFloat()) }
    var checked by remember { mutableStateOf(device.isPoweredOn) }
    val fixedColor = fixColor(device.color, isSystemInDarkTheme())
    val deviceColor = Color(fixedColor)
    // decorationColor is the fixedColor with some transparency
    val deviceDecorationColor = Color(
        android.graphics.Color.argb(90, android.graphics.Color.red(fixedColor), android.graphics.Color.green(fixedColor), android.graphics.Color.blue(fixedColor))
    )

    Card(
        modifier = modifier
            .padding(6.dp)
            .clip(CardDefaults.shape)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier, verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(device.name, style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.padding(bottom = 2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            device.address,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            painter = painterResource(R.drawable.twotone_signal_wifi_2_bar_24),
                            contentDescription = stringResource(R.string.description_back_button),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .height(20.dp)
                        )
                        if (!device.isOnline) {
                            Text(
                                stringResource(R.string.is_offline),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = checked,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = deviceColor,
                        checkedTrackColor = deviceDecorationColor,
                        uncheckedThumbColor = deviceColor,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                        uncheckedBorderColor = deviceDecorationColor,
                    ),
                    onCheckedChange = {
                        checked = it
                    }
                )
            }
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = deviceColor,
                    activeTrackColor = deviceColor,
                ),
                onValueChangeFinished = {
                    // this is called when the user completed selecting the value
                },
            )
        }
    }
}

@Preview
@Composable
fun DeviceListItemPreview() {
    val device = Device(
        address = "192.168.100.250",
        name = "Preview Device",
        isCustomName = false,
        isHidden = true,
        macAddress = "1A:2B:3C:4D:5E:6F",
        brightness = 127,
        color = Color.Cyan.toArgb(),
        isPoweredOn = true,
        isOnline = true,
        isRefreshing = false,
        networkRssi = -75,
        networkSignal = 70,
        isEthernet = false,
        newUpdateVersionTagAvailable = ""
    )
    DeviceListItem(device = device)
}

/**
 * Fixes the color if it is too dark or too bright depending of the dark/light theme
 */
private fun fixColor(color: Int, isDarkTheme: Boolean): Int {
    val floatArray = FloatArray(3)
    ColorUtils.colorToHSL(color, floatArray)

    if (isDarkTheme && floatArray[2] < 0.25f) {
        floatArray[2] = 0.25f
    } else if (!isDarkTheme && floatArray[2] > 0.6f) {
        floatArray[2] = 0.6f
    }

    return ColorUtils.HSLToColor(floatArray)
}
