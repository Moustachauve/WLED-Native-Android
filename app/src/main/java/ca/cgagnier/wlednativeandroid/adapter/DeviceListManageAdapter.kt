package ca.cgagnier.wlednativeandroid.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemManageBinding
import ca.cgagnier.wlednativeandroid.model.Device


class DeviceListManageAdapter(
    private val onItemClicked: (Device) -> Unit,
    private val onItemEditClicked: (Device) -> Unit,
    private val onItemDeleteClicked: (Device) -> Unit
) : AbstractDeviceListAdapter<DeviceListManageAdapter.DeviceListViewHolder>(DiffCallback) {

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

    companion object {
        protected val DiffCallback = object : DiffUtil.ItemCallback<Device>() {
            override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem.address == newItem.address
            }
            override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
                if (oldItem == newItem)
                    return true

                return oldItem.address == newItem.address
                        && oldItem.name == newItem.name
                        && oldItem.isCustomName == newItem.isCustomName
                        && oldItem.isHidden == newItem.isHidden
            }
        }
    }
}