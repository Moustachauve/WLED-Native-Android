package ca.cgagnier.wlednativeandroid

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.fragment.DeviceViewFragment

abstract class AbstractDeviceListAdapter<VH : AbstractDeviceListAdapter.ViewHolder>(
    protected var deviceList: ArrayList<DeviceItem>
) :
    ListAdapter<DeviceItem, VH>(DiffCallback) {

    abstract class ViewHolder(itemBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        abstract fun bindItem(device: DeviceItem)
    }

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
        return deviceList[position].address.hashCode().toLong()
    }

    override fun getItemCount() = deviceList.count()

    fun switchContent(id: Int, fragment: Fragment) {
        if (context is MainActivity) {
            val mainActivity = context as MainActivity
            mainActivity.switchContent(id, fragment, DeviceViewFragment.TAG_NAME)
        }
    }

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

    @SuppressLint("NotifyDataSetChanged")
    fun replaceItems(items: ArrayList<DeviceItem>) {
        deviceList = items
        notifyDataSetChanged()
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DeviceItem>() {
            override fun areItemsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean {
                return oldItem.isSame(newItem)
            }

            override fun areContentsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}