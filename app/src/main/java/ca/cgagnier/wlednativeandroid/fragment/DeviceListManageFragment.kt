package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.*
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository


class DeviceListManageFragment : Fragment(R.layout.fragment_device_list_manage),
    DeviceRepository.DataChangedListener {

    private val deviceListAdapter = DeviceListManageAdapter(ArrayList(DeviceRepository.getAll()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceRepository.registerDataChangedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DeviceRepository.unregisterDataChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deviceListRecyclerView = view.findViewById<RecyclerView>(R.id.device_list_recycler_view)
        val emptyDataView = view.findViewById<ConstraintLayout>(R.id.empty_data_parent)
        val layoutManager = LinearLayoutManager(view.context)

        deviceListRecyclerView.adapter = deviceListAdapter
        deviceListRecyclerView.layoutManager = layoutManager
        deviceListRecyclerView.setHasFixedSize(true)

        val dividerItemDecoration = DividerItemDecoration(
            deviceListRecyclerView.context,
            layoutManager.orientation
        )
        deviceListRecyclerView.addItemDecoration(dividerItemDecoration)

        val emptyDataObserver = EmptyDataObserver(deviceListRecyclerView, emptyDataView)
        deviceListAdapter.registerAdapterDataObserver(emptyDataObserver)

        val findMyDeviceButton = view.findViewById<Button>(R.id.find_my_device_button)
        findMyDeviceButton.setOnClickListener {
            val fragment = DeviceDiscoveryFragment()
            switchContent(R.id.fragment_container_view, fragment)
        }
    }

    override fun onItemChanged(item: DeviceItem) {
        deviceListAdapter.itemChanged(item)
    }

    override fun onItemAdded(item: DeviceItem) {
        deviceListAdapter.addItem(item)
    }

    override fun onItemRemoved(item: DeviceItem) {
        deviceListAdapter.removeItem(item)
    }

    private fun switchContent(id: Int, fragment: Fragment) {
        if (context is MainActivity) {
            val mainActivity = context as MainActivity
            mainActivity.switchContent(id, fragment)
        }
    }
}