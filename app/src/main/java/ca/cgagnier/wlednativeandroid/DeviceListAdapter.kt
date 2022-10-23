package ca.cgagnier.wlednativeandroid

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemBinding
import ca.cgagnier.wlednativeandroid.fragment.DeviceViewFragment
import ca.cgagnier.wlednativeandroid.model.JsonPost
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.service.ThrottleApiPostCall


class DeviceListAdapter(deviceList: ArrayList<DeviceItem>) :
    AbstractDeviceListAdapter<DeviceListAdapter.DeviceListViewHolder>(deviceList) {

    inner class DeviceListViewHolder(val itemBinding: DeviceListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(device:DeviceItem) {
            itemBinding.nameTextView.text = if (device.name == "") context.getString(R.string.default_device_name) else device.name
            itemBinding.ipAddressTextView.text = device.address
            itemBinding.isOffline.visibility = if (device.isOnline) View.INVISIBLE else View.VISIBLE
            itemBinding.brightnessSeekbar.progress = device.brightness
            itemBinding.powerStatusSwitch.isChecked = device.isPoweredOn
            itemBinding.networkStatus.setImageResource(device.getNetworkStrengthImage())

            setSeekBarColor(itemBinding.brightnessSeekbar, device.color)
            setSwitchColor(itemBinding.powerStatusSwitch, device.color)

            itemBinding.refreshProgressBar.visibility = if (device.isRefreshing) View.VISIBLE else View.GONE
            itemBinding.powerStatusSwitch.visibility = if (device.isRefreshing) View.INVISIBLE else View.VISIBLE

            itemBinding.container.setOnClickListener {
                fragmentJump(device)
            }

            itemBinding.powerStatusSwitch.setOnClickListener {
                val deviceSetPost = JsonPost(isOn = !device.isPoweredOn)
                DeviceApi.postJson(device, deviceSetPost)
            }

            itemBinding.brightnessSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
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
        return DeviceListViewHolder(DeviceListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun updateView(holder: DeviceListViewHolder, currentItem: DeviceItem) {
        holder.bindItem(currentItem)
    }

    override fun getItemCount() = deviceList.count()

    private fun fragmentJump(item: DeviceItem) {
        val fragment = DeviceViewFragment.newInstance(item)
        switchContent(R.id.fragment_container_view, fragment)
    }

    /**
     * Fixes the color if it is too dark or too bright depending of the dark/light theme
     */
    private fun fixColor(color: Int): Int {
        val floatArray = FloatArray(3)
        ColorUtils.colorToHSL(color, floatArray)

        if (isDark() && floatArray[2] < 0.25f) {
            floatArray[2] = 0.25f
        } else if (!isDark() && floatArray[2] > 0.8f) {
            floatArray[2] = 0.8f
        }

        return ColorUtils.HSLToColor(floatArray)
    }

    private fun setSeekBarColor(seekBar: SeekBar, color: Int) {
        val fixedColor = fixColor(color)

        seekBar.thumb.setTint(fixedColor)
        seekBar.progressDrawable.setTint(fixedColor)
    }

    private fun setSwitchColor(switch: SwitchCompat, color: Int) {
        val fixedColor = fixColor(color)

        // trackColor is the thumbColor with some transparency
        val trackColor: Int = Color.argb(90, Color.red(fixedColor), Color.green(fixedColor), Color.blue(fixedColor))

        DrawableCompat.setTintList(
            switch.thumbDrawable, ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                    fixedColor,
                    Color.WHITE
                )
            )
        )
        // setting the track color
        DrawableCompat.setTintList(
            switch.trackDrawable, ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                    trackColor,
                    ResourcesCompat.getColor(context.resources, R.color.light_gray_semi_transparent, null)
                )
            )
        )
    }

    private fun isDark(): Boolean {
        return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}