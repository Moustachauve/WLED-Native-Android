package ca.cgagnier.wlednativeandroid


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.fragment.DeviceEditFragment
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DeviceListManageAdapter(deviceList: ArrayList<DeviceItem>) : AbstractDeviceListAdapter<DeviceListManageAdapter.DeviceListViewHolder>(deviceList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.device_list_item_manage, parent, false)
        return DeviceListViewHolder(itemView)
    }

    override fun updateView(holder: DeviceListViewHolder, currentItem: DeviceItem) {
        holder.apply {
            holder.nameTextView.text = if (currentItem.name == "") context.getString(R.string.default_device_name) else currentItem.name
            holder.ipAddressTextView.text = currentItem.address

            val hiddenVisibility = if (currentItem.isHidden) View.VISIBLE else View.GONE
            holder.hiddenImage.visibility = hiddenVisibility
            holder.hiddenText.visibility = hiddenVisibility

            val activity = holder.itemView.context as AppCompatActivity

            holder.container.setOnClickListener {
                openEditDialog(currentItem, activity.supportFragmentManager)
            }

            holder.editButton.setOnClickListener {
                openEditDialog(currentItem, activity.supportFragmentManager)
            }

            holder.deleteButton.setOnClickListener {
                deleteItem(currentItem)
            }
        }
    }

    class DeviceListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ip_address_text_view)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        val hiddenImage: ImageView = itemView.findViewById(R.id.hidden_image)
        val hiddenText: TextView = itemView.findViewById(R.id.hidden_text)
    }

    private fun openEditDialog(item: DeviceItem, fragmentManager: FragmentManager) {
        val dialog = DeviceEditFragment.newInstance(item)
        dialog.showsDialog = true
        dialog.show(fragmentManager, "device_add_manually")
    }

    private fun deleteItem(item: DeviceItem) {
        val name = if (item.name != "") item.name else context.getString(R.string.default_device_name)

        MaterialAlertDialogBuilder(context, R.style.Theme_WLEDNativeAndroid_Dialog_Alert)
            .setTitle(context.getString(R.string.remove_device_confirm))
            .setMessage(context.getString(R.string.remove_device_confirm_text, name, item.address))
            .setPositiveButton(R.string.remove) { _, _ ->
                DeviceRepository.remove(item)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}