package ca.cgagnier.wlednativeandroid.adapter

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.DeviceItem

abstract class AbstractDeviceListAdapter<VH : AbstractDeviceListAdapter.ViewHolder>(
    protected var deviceList: ArrayList<DeviceItem>
) :
    ListAdapter<DeviceItem, VH>(DiffCallback) {

    abstract class ViewHolder(itemBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        abstract fun bindItem(device: DeviceItem)
    }

    var isSelectable: Boolean = false
    protected var _selectedIndex = -1
    protected lateinit var context: Context

    init {
        this.setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindItem(deviceList[position])
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            // refresh all
            onBindViewHolder(holder, position)
            return
        }
        holder.bindItem(deviceList[position])
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun getItemId(position: Int): Long {
        return deviceList[position].hashCode().toLong()
    }

    override fun getItemCount() = deviceList.count()

    fun getAllItems(): ArrayList<DeviceItem> {
        return deviceList
    }

    protected fun getItemPosition(item: DeviceItem): Int? {
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
            notifyItemChanged(position, "updated")
        }
    }

    fun addItem(item: DeviceItem): Int {
        deviceList.add(item)
        // TODO(SORTING) Improve handling of sorting (use submitList?)
        deviceList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        notifyItemInserted(deviceList.size - 1)
        return deviceList.size - 1
    }

    fun removeItem(item: DeviceItem) {
        val position = getItemPosition(item)
        if (position != null) {
            deviceList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getSelectedIndex(): Int {
        return _selectedIndex
    }

    fun setSelectedIndex(index: Int) {
        val oldPosition = _selectedIndex
        _selectedIndex = index
        notifyItemChanged(oldPosition)
        notifyItemChanged(index)
    }

    fun replaceItems(items: List<DeviceItem>) {
        deviceList = ArrayList(items)
        // TODO(SORTING) Improve handling of sorting (use submitList?)
        deviceList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        notifyDataSetChanged()
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DeviceItem>() {
            override fun areItemsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean {
                return oldItem.isSame(newItem)
            }
        }
    }
}