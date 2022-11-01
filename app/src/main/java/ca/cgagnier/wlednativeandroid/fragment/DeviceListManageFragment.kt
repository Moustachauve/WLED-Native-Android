package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.*
import ca.cgagnier.wlednativeandroid.adapter.DeviceListManageAdapter
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceListManageBinding
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DeviceViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DeviceListManageFragment : DialogFragment(),
    DeviceRepository.DataChangedListener {

    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private lateinit var deviceListAdapter: DeviceListManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceRepository.registerDataChangedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DeviceRepository.unregisterDataChangedListener(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentDeviceListManageBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(binding.root.context)

        deviceListAdapter =
            DeviceListManageAdapter(ArrayList(DeviceRepository.getAll())) { deviceItem: DeviceItem ->
                deviceViewModel.updateCurrentDevice(deviceItem)
                deviceViewModel.updateSelectedIndex(-1)
                dismiss()
            }

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
            val dialog = DeviceDiscoveryFragment()
            dialog.showsDialog = true
            dialog.show(childFragmentManager, "device_discovery")
        }


        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder
            .setTitle(R.string.manage_devices)
            .setPositiveButton(getString(R.string.add_device_manually), null)
            .setNeutralButton(R.string.close, null)
            .setView(binding.root)
        return builder.create()
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
}