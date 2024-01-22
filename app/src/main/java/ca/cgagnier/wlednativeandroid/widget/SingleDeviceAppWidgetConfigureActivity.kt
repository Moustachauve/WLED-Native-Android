package ca.cgagnier.wlednativeandroid.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.cgagnier.wlednativeandroid.MainActivity
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DevicesDatabase
import ca.cgagnier.wlednativeandroid.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The configuration screen for the [SingleDeviceAppWidget] AppWidget.
 */
@RequiresApi(Build.VERSION_CODES.S)
class SingleDeviceAppWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val database = DevicesDatabase.getDatabase(applicationContext)
        val deviceRepository = DeviceRepository(database)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            val devices by deviceRepository.allDevices.collectAsStateWithLifecycle(listOf())
            AppTheme {
                DeviceSelection(devices)
            }
        }
    }

    fun onClick(device: Device) {
        val context = this@SingleDeviceAppWidgetConfigureActivity

        // When the button is clicked, store the string locally
        saveTitlePref(context, appWidgetId, device.macAddress)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            SingleDeviceAppWidget.updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    @Composable
    fun DeviceSelection(devices: List<Device>) {
        Surface {
            if (devices.isEmpty()) {
                EmptyDeviceList()
            } else {
                Column {
                    Text(
                        "Select a Device",
                        Modifier.padding(6.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    devices.forEach { device ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onClick(device) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                Modifier.width(IntrinsicSize.Max)
                            ) {
                                Text(
                                    device.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp
                                )
                                Text(device.address, fontSize = 12.sp)
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Select")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun EmptyDeviceList() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.fillMaxHeight(0.15f))
            Image(
                painter = painterResource(id = R.drawable.akemi_018_teeth),
                contentDescription = stringResource(id = R.string.awkward_akemi_character),
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .heightIn(0.dp, 100.dp)
            )
            Text(
                stringResource(id = R.string.you_dont_have_any_devices_yet),
                modifier = Modifier.padding(12.dp)
            )
            Button(onClick = {
                val intent = Intent(
                    applicationContext,
                    MainActivity::class.java
                )
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(intent)
            }) {
                Text(stringResource(R.string.manage_devices))
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun DeviceSelectionPreview() {
        AppTheme {
            DeviceSelection(
                listOf(
                    Device(
                        name = "device 1",
                        address = "192.168.1.1",
                        isCustomName = false,
                        isHidden = false,
                        macAddress = "123"
                    ),
                    Device(
                        name = "device 2 - longer",
                        address = "192.168.1.2",
                        isCustomName = false,
                        isHidden = false,
                        macAddress = "456"
                    ),
                    Device(
                        name = "device 3",
                        address = "192.168.1.3",
                        isCustomName = false,
                        isHidden = true,
                        macAddress = "789"
                    ),
                )
            )
        }
    }


    @Preview(showBackground = true, showSystemUi = true)
    @Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun DeviceSelectionNonePreview() {
        AppTheme {
            DeviceSelection(listOf())
        }
    }
}


private const val PREFS_NAME = "ca.cgagnier.wlednativeandroid.SingleDeviceAppWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: context.getString(R.string.appwidget_text)
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}