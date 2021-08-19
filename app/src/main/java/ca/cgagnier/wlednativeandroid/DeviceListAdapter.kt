package ca.cgagnier.wlednativeandroid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.fragment.DeviceViewFragment

class DeviceListAdapter(private val deviceList: ArrayList<DeviceItem>) : RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder>() {

    private lateinit var context: Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

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

        holder.container.setOnClickListener {
            fragmentJump(currentItem)
        }
    }

    private fun fragmentJump(item: DeviceItem) {
        val fragment = DeviceViewFragment()
        val bundle = Bundle()
        //bundle.putParcelable("item_selected_key", item)
        fragment.arguments = bundle
        switchContent(R.id.fragment_container_view, fragment)
    }

    fun switchContent(id: Int, fragment: Fragment) {
        if (context is MainActivity) {
            val mainActivity = context as MainActivity
            mainActivity.switchContent(id, fragment, DeviceViewFragment.TAG_NAME)
        }
    }

    override fun getItemCount() = deviceList.count()

    class DeviceListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ip_address_text_view)
        val isOfflineTextView: TextView = itemView.findViewById(R.id.is_offline)
        val brightnessSeekBar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val powerStatusSwitch: SwitchCompat = itemView.findViewById(R.id.power_status_switch)
    }

    private fun getItemPosition(item: DeviceItem): Int? {
        for (i in 0 until deviceList.size) {
            if (item == deviceList[i]) {
                return i
            }
        }
        return null
    }

    fun itemChanged(item: DeviceItem) {
        val position = getItemPosition(item)
        if (position != null) {
            deviceList[position] = item
            notifyItemChanged(position)
        }
    }

    fun addItem(item: DeviceItem) {
        deviceList.add(item)
        notifyItemInserted(deviceList.size - 1)
    }

    fun removeItem(item: DeviceItem) {
        val position = getItemPosition(item)
        if (position != null) {
            deviceList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}