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

class DeviceListAdapter(private val deviceList: List<DeviceListItem>) : RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder>() {

    private lateinit var context: Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

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

        holder.container.setOnClickListener {
            fragmentJump(currentItem)
        }
    }

    private fun fragmentJump(item: DeviceListItem) {
        val fragment = DeviceViewFragment()
        val bundle = Bundle()
        //bundle.putParcelable("item_selected_key", item)
        fragment.arguments = bundle
        switchContent(R.id.fragment_container_view, fragment)
    }

    fun switchContent(id: Int, fragment: Fragment) {
        if (context is MainActivity) {
            val mainActivity = context as MainActivity
            mainActivity.switchContent(id, fragment)
        }
    }

    override fun getItemCount() = deviceList.size

    class DeviceListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ip_address_text_view)
        val brightnessSeekBar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val powerStatusSwitch: SwitchCompat = itemView.findViewById(R.id.power_status_switch)
    }
}