package ca.cgagnier.wlednativeandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemFoundBinding


class DeviceListFoundAdapter(
    deviceList: ArrayList<DeviceItem>,
    private val onItemClicked: (DeviceItem, Int) -> Unit
) :
    AbstractDeviceListAdapter<DeviceListFoundAdapter.DeviceListViewHolder>(deviceList) {

    inner class DeviceListViewHolder(private val itemBinding: DeviceListItemFoundBinding) :
        ViewHolder(itemBinding) {
        override fun bindItem(device: DeviceItem) {
            itemBinding.nameTextView.text =
                if (device.name == "") context.getString(R.string.default_device_name) else device.name
            itemBinding.ipAddressTextView.text = device.address
            itemBinding.networkStatus.setImageResource(device.getNetworkStrengthImage())

            itemBinding.container.setOnClickListener {
                onItemClicked(device, bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        return DeviceListViewHolder(
            DeviceListItemFoundBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}