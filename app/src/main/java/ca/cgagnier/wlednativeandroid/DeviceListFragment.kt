package ca.cgagnier.wlednativeandroid

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DeviceListFragment : Fragment(R.layout.fragment_device_list) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val deviceList = loadDeviceList()
        val deviceListRecyclerView = view.findViewById<RecyclerView>(R.id.device_list_recycler_view)
        val layoutManager = LinearLayoutManager(view.context)

        deviceListRecyclerView.adapter = DeviceListAdapter(deviceList)
        deviceListRecyclerView.layoutManager = layoutManager
        deviceListRecyclerView.setHasFixedSize(true)

        val dividerItemDecoration = DividerItemDecoration(
            deviceListRecyclerView.context,
            layoutManager.orientation
        )
        deviceListRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home, menu)

        val actionBar = activity?.actionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_device_add -> {
                openAddDeviceFragment()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openAddDeviceFragment() {
        val fragment = DeviceAddFragment()
        switchContent(R.id.fragment_container_view, fragment)
    }

    fun switchContent(id: Int, fragment: Fragment) {
        if (context is MainActivity) {
            val mainActivity = context as MainActivity
            mainActivity.switchContent(id, fragment)
        }
    }

    private fun loadDeviceList(): List<DeviceListItem> {
        val list = ArrayList<DeviceListItem>()

        for (i in 0 until 66) {
            val fakeIp = i + 104
            val item = DeviceListItem("Wled $i", "192.168.1.$fakeIp", 127, i % 3 <= 1)
            list += item
        }

        return list
    }
}