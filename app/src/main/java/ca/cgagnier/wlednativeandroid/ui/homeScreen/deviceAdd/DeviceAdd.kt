package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.ui.components.DeviceVisibleSwitch


@Composable
fun DeviceAdd(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    deviceAdded: () -> Unit,
    viewModel: DeviceAddViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.validationEvents.collect { event ->
            when (event) {
                is DeviceAddViewModel.ValidationEvent.Success -> {
                    deviceAdded()
                }
            }
        }
    }
    LaunchedEffect(sheetState) {
        if (!sheetState.isVisible || sheetState.targetValue == SheetValue.Hidden) {
            viewModel.clear()
        }
    }
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            val focusRequester = remember {
                FocusRequester()
            }
            val focusManager = LocalFocusManager.current

            LaunchedEffect("initialFocus") {
                focusRequester.requestFocus()
            }
            Text(
                text = stringResource(R.string.add_a_device),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = state.address,
                onValueChange = {
                    viewModel.onEvent(DeviceAddFormEvent.AddressChanged(it))
                },
                label = { Text(stringResource(R.string.ip_address_or_url)) },
                isError = state.addressError != null,
                supportingText = {
                    if (state.addressError != null) {
                        Text(stringResource(state.addressError))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Uri,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(Modifier.padding(4.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = {
                    viewModel.onEvent(DeviceAddFormEvent.NameChanged(it))
                },
                label = { Text(stringResource(R.string.custom_name)) },
                supportingText = { Text(stringResource(R.string.leave_this_empty_to_use_the_device_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.onEvent(DeviceAddFormEvent.Submit)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(Modifier.padding(4.dp))

            DeviceVisibleSwitch(
                isHidden = state.isHidden,
                onCheckedChange = {
                    viewModel.onEvent(DeviceAddFormEvent.IsHiddenChanged(it))
                }
            )
            Spacer(
                Modifier
                    .height(16.dp)
                    .weight(1f)
            )
            Button(
                onClick = {
                    viewModel.onEvent(DeviceAddFormEvent.Submit)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.description_back_button)
                )
                Text(stringResource(R.string.add))
            }
    }
}