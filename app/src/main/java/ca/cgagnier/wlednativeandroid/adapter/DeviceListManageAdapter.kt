package ca.cgagnier.wlednativeandroid.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.DeviceListItemManageBinding
import ca.cgagnier.wlednativeandroid.fragment.DeviceEditFragment
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DeviceListManageAdapter(
    deviceList: ArrayList<DeviceItem>,
    private val onItemClicked: (DeviceItem) -> Unit
) :
    AbstractDeviceListAdapter<DeviceListManageAdapter.DeviceListViewHolder>(
        deviceList
    ) {

    inner class DeviceListViewHolder(private val itemBinding: DeviceListItemManageBinding) :
        ViewHolder(itemBinding) {
        override fun bindItem(device: DeviceItem) {
            itemBinding.nameTextView.text =
                if (device.name == "") context.getString(R.string.default_device_name) else device.name
            itemBinding.ipAddressTextView.text = device.address

            itemBinding.hiddenGroup.visibility = if (device.isHidden) View.VISIBLE else View.GONE

            val activity = itemView.context as AppCompatActivity

            itemBinding.container.setOnClickListener {
                onItemClicked(device)
                openEditDialog(device, activity.supportFragmentManager)
            }

            itemBinding.editButton.setOnClickListener {
                openEditDialog(device, activity.supportFragmentManager)
            }

            itemBinding.deleteButton.setOnClickListener {
                deleteItem(device)
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

    private fun openEditDialog(item: DeviceItem, fragmentManager: FragmentManager) {
        val dialog = DeviceEditFragment.newInstance(item)
        dialog.showsDialog = true
        dialog.show(fragmentManager, "device_add_manually")
    }

    private fun deleteItem(item: DeviceItem) {
        val name =
            if (item.name != "") item.name else context.getString(R.string.default_device_name)

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.remove_device_confirm))
            .setMessage(context.getString(R.string.remove_device_confirm_text, name, item.address))
            .setPositiveButton(R.string.remove) { _, _ ->
                DeviceRepository.remove(item)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}