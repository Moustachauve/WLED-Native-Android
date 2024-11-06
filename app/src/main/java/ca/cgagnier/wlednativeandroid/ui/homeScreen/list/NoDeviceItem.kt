package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.cgagnier.wlednativeandroid.R

@Composable
fun NoDevicesItem(
    modifier: Modifier = Modifier,
    onAddDevice: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 24.dp),
            horizontalAlignment = CenterHorizontally
        ) {
            Image(
                modifier = Modifier.heightIn(0.dp, 80.dp),
                painter = painterResource(id = R.drawable.akemi_018_teeth),
                contentDescription = stringResource(R.string.awkward_akemi_character)
            )
            Text(stringResource(R.string.you_dont_have_any_devices_yet))
            Button(
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                onClick = onAddDevice,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_a_device),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Text(stringResource(R.string.add_a_device))
            }
        }
    }
}