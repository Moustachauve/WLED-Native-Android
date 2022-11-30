package ca.cgagnier.wlednativeandroid.adapter

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.model.Device

abstract class AbstractDeviceListAdapter<VH : AbstractDeviceListAdapter.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<Device> = DiffCallback) : ListAdapter<Device, VH>(diffCallback) {

    abstract class ViewHolder(itemBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        abstract fun bindItem(device: Device)
    }

    var isSelectable: Boolean = false
    protected var selectedDevice: Device? = null
    protected lateinit var context: Context

    init {
        this.setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val current = getItem(position)
        holder.bindItem(current)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).address.hashCode().toLong()
    }

    protected fun getItemPosition(item: Device): Int {
        for ((index, value) in currentList.withIndex()) {
            if (item.address == value.address) {
                return index
            }
        }

        return -1
    }

    // Returns the selectedIndex
    fun setSelectedDevice(device: Device): Int {
        val oldSelectedIndex = selectedDevice?.let { getItemPosition(it) }
        selectedDevice = device
        val selectedIndex = getItemPosition(device)
        oldSelectedIndex?.let { notifyItemChanged(it) }
        notifyItemChanged(selectedIndex)
        return selectedIndex
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
                        && oldItem.isOnline == newItem.isOnline
                        && oldItem.isRefreshing == newItem.isRefreshing
                        && oldItem.brightness == newItem.brightness
                        && oldItem.color == newItem.color
                        && oldItem.getNetworkStrengthImage() == newItem.getNetworkStrengthImage()
            }
        }
    }
}