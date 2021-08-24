package ca.cgagnier.wlednativeandroid

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.fragment.DeviceViewFragment
import ca.cgagnier.wlednativeandroid.model.JsonPost
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.service.ThrottleApiPostCall
import android.content.res.ColorStateList
import android.content.res.Configuration
import androidx.core.content.res.ResourcesCompat

import androidx.core.graphics.drawable.DrawableCompat

import androidx.core.graphics.ColorUtils


class DeviceListAdapter(deviceList: ArrayList<DeviceItem>) : AbstractDeviceListAdapter<DeviceListAdapter.DeviceListViewHolder>(deviceList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.device_list_item, parent, false)
        return DeviceListViewHolder(itemView)
    }

    override fun updateView(holder: DeviceListViewHolder, currentItem: DeviceItem) {
        holder.apply {
            nameTextView.text = if (currentItem.name == "") context.getString(R.string.default_device_name) else currentItem.name
            ipAddressTextView.text = currentItem.address
            isOfflineTextView.visibility = if (currentItem.isOnline) View.INVISIBLE else View.VISIBLE
            brightnessSeekBar.progress = currentItem.brightness
            powerStatusSwitch.isChecked = currentItem.isPoweredOn
            networkStatusImage.setImageResource(currentItem.getNetworkStrengthImage())

            setSeekBarColor(brightnessSeekBar, currentItem.color)
            setSwitchColor(powerStatusSwitch, currentItem.color)

            refreshProgressBar.visibility = if (currentItem.isRefreshing) View.VISIBLE else View.GONE
            powerStatusSwitch.visibility = if (currentItem.isRefreshing) View.INVISIBLE else View.VISIBLE

            container.setOnClickListener {
                fragmentJump(currentItem)
            }

            powerStatusSwitch.setOnClickListener {
                val deviceSetPost = JsonPost(isOn = !currentItem.isPoweredOn)
                DeviceApi.postJson(currentItem, deviceSetPost)
            }

            brightnessSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        ThrottleApiPostCall.send(currentItem, JsonPost(brightness = value))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    currentItem.isSliding = true
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    currentItem.isSliding = false
                }

            })
        }
    }

    override fun getItemCount() = deviceList.count()

    class DeviceListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ip_address_text_view)
        val networkStatusImage: ImageView = itemView.findViewById(R.id.network_status)
        val isOfflineTextView: TextView = itemView.findViewById(R.id.is_offline)
        val brightnessSeekBar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val powerStatusSwitch: SwitchCompat = itemView.findViewById(R.id.power_status_switch)
        val refreshProgressBar: ProgressBar = itemView.findViewById(R.id.refresh_progress_bar)
    }

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