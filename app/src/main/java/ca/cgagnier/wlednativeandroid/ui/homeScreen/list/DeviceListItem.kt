package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import SliderWithLabel
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.ui.components.deviceName
import ca.cgagnier.wlednativeandroid.ui.theme.DeviceTheme
import kotlin.math.roundToInt

@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    device: Device,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    swipeToDismissBoxState: SwipeToDismissBoxState,
    onPowerSwitchToggle: (isOn: Boolean) -> Unit = {},
    onBrightnessChanged: (brightness: Int) -> Unit = {},
) {
    var checked by remember(device.isPoweredOn) { mutableStateOf(device.isPoweredOn) }
    val haptic = LocalHapticFeedback.current

    DeviceTheme(device) {
        val cardColor =
            if (isSelected) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceContainer

        SwipeBox(
            modifier = modifier,
            device = device,
            swipeToDismissBoxState = swipeToDismissBoxState
        ) {
            SelectableCard(
                modifier = Modifier
                    .padding(bottom = 6.dp)
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
                        DeviceInfoTwoRows(
                            device = device,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            modifier = Modifier.padding(start = 10.dp),
                            checked = checked,
                            onCheckedChange = { isOn ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                checked = isOn
                                onPowerSwitchToggle(isOn)
                            }
                        )
                    }
                    BrightnessSlider(device, onBrightnessChanged)
                }
            }
        }
    }
}

@Composable
private fun BrightnessSlider(
    device: Device,
    onBrightnessChanged: (brightness: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var sliderPosition by remember(device.brightness) { mutableFloatStateOf(device.brightness.toFloat()) }
    SliderWithLabel(
        value = if (device.isOnline) sliderPosition else 0f,
        enabled = device.isOnline,
        onValueChange = {
            if (it.roundToInt() != sliderPosition.roundToInt()) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            sliderPosition = it
        },
        valueRange = 1f..255f,
        onValueChangeFinished = {
            onBrightnessChanged(sliderPosition.roundToInt())
        },
    )
}

@Composable
private fun SwipeBox(
    modifier: Modifier = Modifier,
    device: Device,
    swipeToDismissBoxState: SwipeToDismissBoxState,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(key1 = swipeToDismissBoxState.targetValue, block = {
        if (swipeToDismissBoxState.progress in 0.01..0.99) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    })

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
                    .padding(bottom = 6.dp)
                    .background(color, shape = CardDefaults.shape)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        val penIcon =
                            if (swipeToDismissBoxState.targetValue == SwipeToDismissBoxValue.StartToEnd) Icons.Filled.Edit else Icons.Outlined.Edit
                        Crossfade(
                            modifier = Modifier.padding(end = 16.dp),
                            targetState = penIcon,
                            label = "pen icon"
                        ) {
                            Icon(
                                imageVector = it,
                                contentDescription = stringResource(R.string.description_back_button),
                            )
                        }
                    }
                    DeviceInfoTwoRows(
                        modifier = Modifier.weight(1f),
                        device = device
                    )
                    if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        val deleteIcon =
                            if (swipeToDismissBoxState.targetValue == SwipeToDismissBoxValue.EndToStart) Icons.Filled.Delete else Icons.Outlined.Delete
                        Crossfade(
                            modifier = Modifier.padding(start = 16.dp),
                            targetState = deleteIcon,
                            label = "delete icon"
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
    device: Device,
    nameMaxLines: Int = 2,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                deviceName(device),
                style = MaterialTheme.typography.titleLarge,
                maxLines = nameMaxLines,
                overflow = TextOverflow.Ellipsis
            )
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
            modifier = Modifier
                .padding(bottom = 2.dp)
                .width(IntrinsicSize.Min),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                device.address,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .width(IntrinsicSize.Max)
            )
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip {
                    if (device.isOnline) {
                        Text(stringResource(R.string.signal_strength, device.networkRssi))
                    } else {
                        Text(stringResource(R.string.signal_strength, stringResource(R.string.is_offline)))
                    }
                } },
                state = rememberTooltipState()
            ) {
                Icon(
                    painter = painterResource(device.getNetworkStrengthImage()),
                    contentDescription = stringResource(R.string.network_status),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(20.dp)
                )
            }
            if (device.hasUpdateAvailable()) {
                Icon(
                    painter = painterResource(R.drawable.baseline_download_24),
                    contentDescription = stringResource(R.string.network_status),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(20.dp)
                )
            }
            if (!device.isOnline) {
                Text(
                    stringResource(R.string.is_offline),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            if (device.isHidden) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_visibility_off_24),
                    contentDescription = stringResource(R.string.description_back_button),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(16.dp)
                )
                Text(
                    stringResource(R.string.hidden_status),
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