package ca.cgagnier.wlednativeandroid.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemManageBinding
import ca.cgagnier.wlednativeandroid.model.Device


class DeviceListManageAdapter(
    private val onItemClicked: (Device) -> Unit,
    private val onItemEditClicked: (Device) -> Unit,
    private val onItemDeleteClicked: (Device) -> Unit
) : AbstractDeviceListAdapter<DeviceListManageAdapter.DeviceListViewHolder>() {

    inner class DeviceListViewHolder(private val itemBinding: DeviceListItemManageBinding) :
        ViewHolder(itemBinding) {
        override fun bindItem(device: Device) {
            itemBinding.nameTextView.text =
                if (device.name == "") context.getString(R.string.default_device_name) else device.name
            itemBinding.ipAddressTextView.text = device.address

            itemBinding.hiddenGroup.visibility = if (device.isHidden) View.VISIBLE else View.GONE

            itemBinding.container.setOnClickListener {
                onItemClicked(device)
            }

            itemBinding.editButton.setOnClickListener {
                onItemEditClicked(device)
            }

            itemBinding.deleteButton.setOnClickListener {
                onItemDeleteClicked(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        return DeviceListViewHolder(
            DeviceListItemManageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}