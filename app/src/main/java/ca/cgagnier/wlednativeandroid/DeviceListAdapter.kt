package ca.cgagnier.wlednativeandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class DeviceListAdapter(private val deviceList: List<DeviceListItem>) : RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.wled_list_item, parent, false)
        return DeviceListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceListViewHolder, position: Int) {
        val currentItem = deviceList[position]
        holder.nameTextView.text = currentItem.name
        holder.ipAddressTextView.text = currentItem.ipAddress
        holder.brightnessSeekBar.progress = currentItem.brightness
        holder.powerStatusSwitch.isChecked = currentItem.isPoweredOn
    }

    override fun getItemCount() = deviceList.size

    class DeviceListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ip_address_text_view)
        val brightnessSeekBar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val powerStatusSwitch: SwitchCompat = itemView.findViewById(R.id.power_status_switch)
    }
}