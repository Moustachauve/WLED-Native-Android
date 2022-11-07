package ca.cgagnier.wlednativeandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemFoundBinding
import ca.cgagnier.wlednativeandroid.model.Device


class DeviceListFoundAdapter(
    private val onItemClicked: (Device, Int) -> Unit
) : AbstractDeviceListAdapter<DeviceListFoundAdapter.DeviceListViewHolder>() {

    inner class DeviceListViewHolder(private val itemBinding: DeviceListItemFoundBinding) :
        ViewHolder(itemBinding) {
        override fun bindItem(device: Device) {
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