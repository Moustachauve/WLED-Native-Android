package ca.cgagnier.wlednativeandroid.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.actions.FloatAction
import android.service.controls.templates.ControlButton
import android.service.controls.templates.RangeTemplate
import android.service.controls.templates.ToggleRangeTemplate
import android.service.controls.templates.ToggleTemplate
import androidx.annotation.RequiresApi
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.StateChangeRequest
import ca.cgagnier.wlednativeandroid.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.jdk9.asPublisher
import kotlinx.coroutines.launch
import java.util.concurrent.Flow
import java.util.function.Consumer
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.R)
@AndroidEntryPoint
class DeviceControlsProviderService : ControlsProviderService() {
    @Inject
    lateinit var repository: DeviceRepository
    @Inject
    lateinit var stateFactory: StateFactory

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        return repository.allDevices
            .take(1)
                .flatMapLatest { deviceList ->
                deviceList.asFlow()
            }.map { device ->
                createStatelessControl(device)
            }.asPublisher()
    }

    override fun createPublisherFor(controlIds: List<String>): Flow.Publisher<Control> {
        val flows = controlIds
            .map { controlId ->
                repository.findLiveDeviceByAddress(controlId)
                    .map { device ->
                        if (device != null)
                            createStatefulControl(device)
                        else
                            createErrorControl(controlId)
                    }
            }

        return flows.merge().asPublisher()
    }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {
        scope.launch {
            // controlId is device address
            repository.findDeviceByAddress(controlId)?.let { device ->
                when (action) {
                    is BooleanAction -> {
                        toggleDevicePower(device, action.newState)
                        consumer.accept(ControlAction.RESPONSE_OK)
                    }
                    is FloatAction -> {
                        // map 0-100% UI range to 0-255 for API
                        val integerBrightness = action.newValue.div(100).times(255).toInt()
                        setDeviceBrightness(device, integerBrightness)
                        consumer.accept(ControlAction.RESPONSE_OK)
                    }

                    else -> consumer.accept(ControlAction.RESPONSE_FAIL)
                }
            } ?: consumer.accept(ControlAction.RESPONSE_FAIL)
        }
    }

    private fun createStatelessControl(device: Device): Control {
        return Control.StatelessBuilder(device.address, createAppIntentForDevice(device))
            .setTitle(device.name)
            .setDeviceType(DeviceTypes.TYPE_LIGHT)
            .build()
    }

    private fun createStatefulControl(device: Device): Control {
        val control = Control.StatefulBuilder(device.address, createAppIntentForDevice(device))
            .setTitle(device.name)
            .setDeviceType(DeviceTypes.TYPE_LIGHT)
            .setStatus(Control.STATUS_OK)

        // set a proper message instead of the ones in Control.STATUS_*
        if (device.isOnline) {
            // set status text based on state
            if (device.isPoweredOn) {
                control.setStatusText(applicationContext.getString(R.string.state_on))
            } else {
                control.setStatusText(applicationContext.getString(R.string.state_off))
            }

            // and add controls
            control.setControlTemplate(
                ToggleRangeTemplate(
                    device.address,
                    ControlButton(
                        device.isPoweredOn,
                        applicationContext.getString(R.string.device_controls_control_button_action_description)
                    ),
                    RangeTemplate(
                        device.address,
                        0f,
                        100f,
                        // map 0-255 range to 0-100% for UI
                        device.brightness.toFloat().times(100).div(255),
                        1f,
                        "%.0f%%"
                    )
                )
            )
        } else {
            // offline
            control.setStatusText(applicationContext.getString(R.string.state_offline))
            // set the template to toggle with forced value of off
            control.setControlTemplate(
                ToggleTemplate(
                    device.address,
                    ControlButton(
                        false,
                        applicationContext.getString(R.string.device_controls_control_button_action_description)
                    )
                )
            )
        }

        return control.build()
    }

    /**
     * Create error like control for use when corresponding device was removed in app
     */
    private fun createErrorControl(controlId: String): Control {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return Control.StatefulBuilder(controlId, pendingIntent)
            .setTitle(applicationContext.getString(R.string.device_controls_device_not_found))
            .setStatus(Control.STATUS_NOT_FOUND)
            .build()
    }

    /**
     * Create [PendingIntent] that launches device specific screen in the app
     */
    private fun createAppIntentForDevice(device: Device): PendingIntent {
        // TODO: we can just remove everything but numbers and be left with a string of a unique integer
        val integerId = device.macAddress.hashCode()

        val intent = Intent(this, MainActivity::class.java)     // TODO: maybe open device specific screen
            .putExtra(EXTRA_DEVICE_MAC, device.macAddress)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val action = PendingIntent.getActivity(
            this,
            integerId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return action
    }

    // (mostly) copied from DeviceListViewModel
    private fun toggleDevicePower(device: Device, isOn: Boolean) {
        val deviceSetPost = JsonPost(isOn = isOn)
        stateFactory.getState(device).requestsManager.addRequest(
            StateChangeRequest(device, deviceSetPost)
        )
    }

    // (mostly) copied from DeviceListViewModel
    private fun setDeviceBrightness(device: Device, brightness: Int) {
        val deviceSetPost = JsonPost(brightness = brightness)
        stateFactory.getState(device).requestsManager.addRequest(
            StateChangeRequest(device, deviceSetPost)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "DeviceControlsProviderService"

        const val EXTRA_DEVICE_MAC = "device_controls_device_mac"
    }
}