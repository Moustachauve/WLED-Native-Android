package ca.cgagnier.wlednativeandroid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DevicesDatabase
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [SingleDeviceAppWidgetConfigureActivity]
 */
@RequiresApi(Build.VERSION_CODES.S)
class SingleDeviceAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.IO).launch {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        if (context == null) {
            Log.w(TAG, "No Context")
            return
        }
        val appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.w(TAG, "No appWidgetId")
            onUpdate(context)
            return
        }
        if (intent.action == ACTION_POWER_BUTTON) {
            CoroutineScope(Dispatchers.IO).launch {
                toggleDevice(context, appWidgetId)
                onUpdate(context)
            }
        } else if (intent.action == ACTION_REFRESH_CLICK) {
            onUpdate(context)
        }
    }

    private suspend fun toggleDevice(context: Context, appWidgetId: Int) {
        val device = loadDevice(context, appWidgetId)
        if (device == null) {
            val deviceAddress = loadTitlePref(context, appWidgetId)
            Log.e(TAG, "Could not load device with address $deviceAddress to toggle")
            return
        }

        val database = DevicesDatabase.getDatabase(context)
        val deviceRepository = DeviceRepository(database)
        val versionWithAssetsRepository = VersionWithAssetsRepository(database)

        val deviceSetPost = JsonPost(isOn = !device.isPoweredOn)
        val deviceApi = DeviceApiService(deviceRepository, ReleaseService(versionWithAssetsRepository))
        deviceApi.postJson(device, deviceSetPost)
    }

    private fun onUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidgetComponentName = ComponentName(
            context.packageName, javaClass.name
        )
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            thisAppWidgetComponentName
        )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        const val TAG = "SingleDeviceAppWidget"
        const val ACTION_POWER_BUTTON = "ca.cgagnier.wled.action.POWER_BUTTON_CLICK"
        const val ACTION_REFRESH_CLICK = "ca.cgagnier.wled.action.REFRESH_CLICK"

        private fun getPendingSelfIntent(
            context: Context,
            appWidgetId: Int,
            action: String
        ): PendingIntent? {
            val intent = Intent(context, SingleDeviceAppWidget::class.java)
            intent.setAction(action)
            Log.d(TAG, "Extra appWidgetId:$appWidgetId")
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            )
        }

        private suspend fun loadDevice(context: Context, appWidgetId: Int): Device? {
            val deviceAddress = loadTitlePref(context, appWidgetId)

            val database = DevicesDatabase.getDatabase(context)
            val deviceRepository = DeviceRepository(database)

            return deviceRepository.findDeviceByAddress(deviceAddress)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        internal suspend fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val device = loadDevice(context, appWidgetId)
            if (device == null) {
                val deviceAddress = loadTitlePref(context, appWidgetId)
                Log.e(TAG, "Could not load device with address $deviceAddress - $appWidgetId")
                val errorView =
                    RemoteViews(context.packageName, R.layout.single_device_app_widget_error)
                errorView.setTextViewText(R.id.appwidget_text, "Error! - $deviceAddress")
                errorView.setOnClickPendingIntent(
                    R.id.power_button,
                    getPendingSelfIntent(context, appWidgetId, ACTION_REFRESH_CLICK)
                )
                appWidgetManager.updateAppWidget(appWidgetId, errorView)
                return
            }

            Log.d(TAG, "Refreshing widget for device ${device.name}")

            val smallView =
                RemoteViews(context.packageName, R.layout.single_device_app_widget_small)
            val tallView = RemoteViews(context.packageName, R.layout.single_device_app_widget_tall)
            val wideView = RemoteViews(context.packageName, R.layout.single_device_app_widget_wide)

            for (view in arrayListOf(smallView, tallView, wideView)) {
                setSharedElements(context, appWidgetId, view, device)
            }

            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(150f, 90f) to smallView,
                SizeF(150f, 150f) to tallView,
                SizeF(200f, 90f) to wideView
            )
            val views = RemoteViews(viewMapping)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun setSharedElements(
            context: Context,
            appWidgetId: Int,
            view: RemoteViews,
            device: Device
        ) {
            view.setTextViewText(R.id.appwidget_text, device.name)

            view.setImageViewResource(
                R.id.power_button,
                if (device.isPoweredOn) R.drawable.akemi_042_cheerful_rgb else R.drawable.akemi_000_off)
            view.setInt(
                R.id.power_button,
                "setBackgroundResource",
                if (device.isPoweredOn) R.drawable.round_button_primary else R.drawable.round_button
            )

            view.setOnClickPendingIntent(
                R.id.power_button,
                getPendingSelfIntent(context, appWidgetId, ACTION_POWER_BUTTON)
            )
        }
    }
}