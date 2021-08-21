package ca.cgagnier.wlednativeandroid

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

class DeviceListAdapter(deviceList: ArrayList<DeviceItem>) : AbstractDeviceListAdapter<DeviceListAdapter.DeviceListViewHolder>(deviceList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.device_list_item, parent, false)
        return DeviceListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceListViewHolder, position: Int) {
        val currentItem = deviceList[position]

        holder.nameTextView.text = if (currentItem.name == "") context.getString(R.string.default_device_name) else currentItem.name
        holder.ipAddressTextView.text = currentItem.address
        holder.isOfflineTextView.visibility = if (currentItem.isOnline) View.INVISIBLE else View.VISIBLE
        holder.brightnessSeekBar.progress = currentItem.brightness
        holder.powerStatusSwitch.isChecked = currentItem.isPoweredOn
        holder.networkStatusImage.setImageResource(currentItem.getNetworkStrengthImage())

        holder.brightnessSeekBar.progressDrawable.setTint(currentItem.color)
        holder.brightnessSeekBar.thumb.setTint(currentItem.color)

        holder.refreshProgressBar.visibility = if (currentItem.isRefreshing) View.VISIBLE else View.GONE
        holder.powerStatusSwitch.visibility = if (currentItem.isRefreshing) View.INVISIBLE else View.VISIBLE

        holder.container.setOnClickListener {
            fragmentJump(currentItem)
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
}