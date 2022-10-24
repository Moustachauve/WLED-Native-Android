package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.*
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceListBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDeviceListBinding.inflate(inflater, container,false)
        val layoutManager = LinearLayoutManager(binding.root.context)

        binding.deviceListRecyclerView.adapter = deviceListAdapter
        binding.deviceListRecyclerView.layoutManager = layoutManager
        binding.deviceListRecyclerView.setHasFixedSize(true)

        val dividerItemDecoration = DividerItemDecoration(
            binding.deviceListRecyclerView.context,
            layoutManager.orientation
        )
        binding.deviceListRecyclerView.addItemDecoration(dividerItemDecoration)

        val emptyDataObserver = EmptyDataObserver(binding.deviceListRecyclerView, binding.emptyDataParent)
        deviceListAdapter.registerAdapterDataObserver(emptyDataObserver)

        binding.emptyDataParent.findMyDeviceButton.setOnClickListener {
            val fragment = DeviceDiscoveryFragment()
            switchContent(R.id.fragment_container_view, fragment)
        }

        return binding.root
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