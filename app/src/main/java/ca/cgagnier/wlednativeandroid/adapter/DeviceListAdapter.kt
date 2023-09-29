package ca.cgagnier.wlednativeandroid.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.AttrRes
import androidx.core.graphics.ColorUtils
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.service.ThrottleApiPostCall
import com.google.android.material.materialswitch.MaterialSwitch


class DeviceListAdapter(
    private val onItemClicked: (Device) -> Unit
) : AbstractDeviceListAdapter<DeviceListAdapter.DeviceListViewHolder>() {

    inner class DeviceListViewHolder(private val itemBinding: DeviceListItemBinding) :
        ViewHolder(itemBinding) {
        override fun bindItem(device: Device) {
            itemBinding.nameTextView.text =
                if (device.name == "") context.getString(R.string.default_device_name) else device.name
            itemBinding.ipAddressTextView.text = device.address
            itemBinding.isOffline.visibility = if (device.isOnline) View.GONE else View.VISIBLE
            itemBinding.updateIndicator.visibility = if (device.hasUpdateAvailable) View.VISIBLE else View.GONE
            itemBinding.updateIndicatorLabel.visibility = itemBinding.updateIndicator.visibility
            itemBinding.brightnessSeekbar.progress = device.brightness
            itemBinding.powerStatusSwitch.isChecked = device.isPoweredOn
            itemBinding.networkStatus.setImageResource(device.getNetworkStrengthImage())

            itemBinding.container.isSelected = isSelectable && selectedDevice?.address == device.address

            setSeekBarColor(itemBinding.brightnessSeekbar, device.color)
            setSwitchColor(itemBinding.powerStatusSwitch, device.color)

            itemBinding.refreshProgressBar.visibility =
                if (device.isRefreshing) View.VISIBLE else View.GONE
            itemBinding.powerStatusSwitch.visibility =
                if (device.isRefreshing) View.INVISIBLE else View.VISIBLE

            itemBinding.container.setOnClickListener {
                onItemClicked(device)
            }

            itemBinding.powerStatusSwitch.setOnClickListener {
                val deviceSetPost = JsonPost(isOn = itemBinding.powerStatusSwitch.isChecked)
                DeviceApi.postJson(device, deviceSetPost)
            }

            itemBinding.brightnessSeekbar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        ThrottleApiPostCall.send(device, JsonPost(brightness = value))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    device.isSliding = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    device.isSliding = false
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        return DeviceListViewHolder(
            DeviceListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Fixes the color if it is too dark or too bright depending of the dark/light theme
     */
    private fun fixColor(color: Int): Int {
        val floatArray = FloatArray(3)
        ColorUtils.colorToHSL(color, floatArray)

        if (isDark() && floatArray[2] < 0.25f) {
            floatArray[2] = 0.25f
        } else if (!isDark() && floatArray[2] > 0.6f) {
            floatArray[2] = 0.6f
        }

        return ColorUtils.HSLToColor(floatArray)
    }

    private fun setSeekBarColor(seekBar: SeekBar, color: Int) {
        val fixedColor = fixColor(color)

        seekBar.thumb.setTint(fixedColor)
        seekBar.progressDrawable.setTint(fixedColor)
    }

    private fun setSwitchColor(switch: MaterialSwitch, color: Int) {
        val fixedColor = fixColor(color)

        // decorationColor is the fixedColor with some transparency
        val decorationColor: Int =
            Color.argb(90, Color.red(fixedColor), Color.green(fixedColor), Color.blue(fixedColor))

        switch.trackTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                fixedColor,
                context.getThemeColor(R.attr.deviceListBackgroundColor)
            )
        )
        switch.trackDecorationTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                decorationColor,
                decorationColor
            )
        )
    }

    private fun isDark(): Boolean {
        return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun Context.getThemeColor(@AttrRes attrRes: Int): Int = TypedValue()
        .apply { theme.resolveAttribute(attrRes, this, true) }
        .data
}