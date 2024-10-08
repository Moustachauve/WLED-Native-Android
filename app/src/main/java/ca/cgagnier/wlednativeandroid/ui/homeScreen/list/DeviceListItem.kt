package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import kotlin.math.roundToInt

@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    device: Device,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    swipeToDismissBoxState: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(),
    onPowerSwitchToggle: (isOn: Boolean) -> Unit = {},
    onBrightnessChanged: (brightness: Int) -> Unit = {},
) {
    var sliderPosition by remember(device.brightness) { mutableFloatStateOf(device.brightness.toFloat()) }
    var checked by remember(device.isPoweredOn) { mutableStateOf(device.isPoweredOn) }

    DeviceTheme(device) {
        val cardColor =
            if (isSelected) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceContainer

        SwipeBox(
            modifier = modifier,
            swipeToDismissBoxState = swipeToDismissBoxState
        ) {
            SelectableCard(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(CardDefaults.shape),
                isSelected = isSelected,
                colors = CardDefaults.cardColors(
                    containerColor = cardColor,
                ),
                onClick = onClick
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier, verticalAlignment = Alignment.CenterVertically
                    ) {
                        DeviceInfoTwoRows(device = device)
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = checked,
                            onCheckedChange = { isOn ->
                                checked = isOn
                                onPowerSwitchToggle(isOn)
                            }
                        )
                    }
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..255f,
                        onValueChangeFinished = {
                            onBrightnessChanged(sliderPosition.roundToInt())
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeBox(
    modifier: Modifier = Modifier,
    swipeToDismissBoxState: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(),
    content: @Composable () -> Unit
) {
    SwipeToDismissBox(
        modifier = modifier,
        state = swipeToDismissBoxState,
        backgroundContent = {
            val color by animateColorAsState(
                when (swipeToDismissBoxState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceDim
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                }, label = "ColorAnimation"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .background(color, shape = CardDefaults.shape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        val deleteIcon =
                            if (swipeToDismissBoxState.targetValue == SwipeToDismissBoxValue.EndToStart) Icons.Filled.Delete else Icons.Outlined.Delete
                        Crossfade(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            targetState = deleteIcon,
                            label = "delete icon"
                        ) {
                            Icon(
                                imageVector = it,
                                contentDescription = stringResource(R.string.description_back_button),
                            )
                        }
                    }
                    if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        val penIcon =
                            if (swipeToDismissBoxState.targetValue == SwipeToDismissBoxValue.StartToEnd) Icons.Filled.Edit else Icons.Outlined.Edit
                        Crossfade(
                            modifier = Modifier.align(Alignment.CenterStart),
                            targetState = penIcon,
                            label = "pen icon"
                        ) {
                            Icon(
                                imageVector = it,
                                contentDescription = stringResource(R.string.description_back_button),
                            )
                        }
                    }
                }
            }
        },
    ) {
        content()
    }
}

@Composable
fun DeviceInfoTwoRows(
    modifier: Modifier = Modifier,
    device: Device
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(device.name, style = MaterialTheme.typography.titleLarge)
            if (device.isRefreshing) {
                val size =
                    (MaterialTheme.typography.titleSmall.lineHeight.value - 4)
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .padding(bottom = 2.dp)
                        .width(size.dp)
                        .height(size.dp),
                )
            }
        }
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
}

@Composable
fun SelectableCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isSelected) {
        OutlinedCard(
            modifier = modifier,
            colors = colors,
            onClick = onClick,
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            onClick = onClick,
        ) {
            content()
        }
    }
}

@Composable
fun DeviceTheme(
    device: Device,
    content: @Composable () -> Unit
) {
    DynamicMaterialTheme(
        seedColor = Color(device.color),
        style = if (device.isOnline) PaletteStyle.Vibrant else PaletteStyle.Neutral,
        animate = true,
        isExtendedFidelity = false,
    ) {
        content()
    }
}