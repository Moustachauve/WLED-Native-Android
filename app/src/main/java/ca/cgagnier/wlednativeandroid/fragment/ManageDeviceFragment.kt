package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.adapter.DeviceListManageAdapter
import ca.cgagnier.wlednativeandroid.databinding.FragmentManageDevicesBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ManageDeviceFragment : DialogFragment() {

    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).repository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository)
    }
    private val manageDevicesViewModel: ManageDevicesViewModel by activityViewModels {
        ManageDevicesViewModelFactory((requireActivity().application as DevicesApplication).repository)
    }

    private lateinit var deviceListAdapter: DeviceListManageAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentManageDevicesBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(binding.root.context)

        deviceListAdapter = DeviceListManageAdapter(
            onItemClicked = { device: Device ->
                deviceListViewModel.updateActiveDevice(device)
                dismiss()
            },
            onItemEditClicked = { device: Device ->
                editItem(device)
            },
            onItemDeleteClicked = { device: Device ->
                deleteItem(device)
            })

        manageDevicesViewModel.allDevices.observe(this) { devices ->
            devices?.let {
                deviceListAdapter.submitList(null)
                deviceListAdapter.submitList(it)
            }
            val isEmpty = devices?.isEmpty() == true
            binding.emptyDataParent.layout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.deviceListRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        binding.deviceListRecyclerView.adapter = deviceListAdapter
        binding.deviceListRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            binding.deviceListRecyclerView.context,
            layoutManager.orientation
        )
        binding.deviceListRecyclerView.addItemDecoration(dividerItemDecoration)

        binding.emptyDataParent.findMyDeviceButton.setOnClickListener {
            val dialog = DiscoverDeviceFragment()
            dialog.showsDialog = true
            dialog.show(childFragmentManager, "device_discovery")
        }

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder
            .setTitle(R.string.manage_devices)
            .setPositiveButton(R.string.close, null)
            .setView(binding.root)
        return builder.create()
    }

    private fun editItem(item: Device) {
        manageDevicesViewModel.updateActiveDevice(item)
        val dialog = DeviceEditFragment()
        dialog.showsDialog = true
        dialog.show(requireActivity().supportFragmentManager, "device_edit")
    }

    private fun deleteItem(item: Device) {
        val context = requireContext()
        val name =
            if (item.name != "") item.name else context.getString(R.string.default_device_name)

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.remove_device_confirm))
            .setMessage(context.getString(R.string.remove_device_confirm_text, name, item.address))
            .setPositiveButton(R.string.remove) { _, _ ->
                manageDevicesViewModel.delete(item)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}