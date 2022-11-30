package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.AutoDiscoveryActivity
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.adapter.DeviceListFoundAdapter
import ca.cgagnier.wlednativeandroid.databinding.FragmentDiscoverDeviceBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import ca.cgagnier.wlednativeandroid.viewmodel.DiscoverDeviceViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DiscoverDeviceViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DiscoverDeviceFragment : DialogFragment(),
    DeviceAddManuallyFragment.NoticeDialogListener,
    DeviceDiscovery.DeviceDiscoveredListener {

    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).repository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository)
    }
    private val discoverDeviceViewModel: DiscoverDeviceViewModel by activityViewModels {
        DiscoverDeviceViewModelFactory((requireActivity().application as DevicesApplication).repository)
    }

    private lateinit var deviceListAdapter: DeviceListFoundAdapter

    private var _binding: FragmentDiscoverDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as DevicesApplication).deviceDiscovery
            .registerDeviceDiscoveredListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity().application as DevicesApplication).deviceDiscovery
            .unregisterDeviceDiscoveredListener(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDiscoverDeviceBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(binding.root.context)

        // Clear the previously discovered devices, otherwise they will still show up the 2nd time
        discoverDeviceViewModel.clear()

        deviceListAdapter = DeviceListFoundAdapter { deviceItem: Device, _: Int ->
            deviceListViewModel.updateActiveDevice(deviceItem)
            dismiss()
        }

        binding.deviceFoundListRecyclerView.adapter = deviceListAdapter
        binding.deviceFoundListRecyclerView.layoutManager = layoutManager
        binding.deviceFoundListRecyclerView.setHasFixedSize(false)

        discoverDeviceViewModel.allDevices.observe(this) { devices ->
            devices?.let {
                deviceListAdapter.submitList(it)
            }
        }

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder
            .setPositiveButton(getString(R.string.add_device_manually), null)
            .setNeutralButton(R.string.close, null)
            .setView(binding.root)
        return builder.create()
    }

    override fun onPause() {
        (requireActivity().application as DevicesApplication).deviceDiscovery.stop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AutoDiscoveryActivity).stopAutoDiscovery()
        (requireActivity().application as DevicesApplication).deviceDiscovery.start()

        val alertDialog = dialog as AlertDialog

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newDialog = DeviceAddManuallyFragment()
            newDialog.show(childFragmentManager, "device_add_manually")
            newDialog.registerDeviceAddedListener(this)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        (requireActivity().application as DevicesApplication).deviceDiscovery.stop()
        (requireActivity().application as DevicesApplication).deviceDiscovery
            .unregisterDeviceDiscoveredListener(this)
        super.onDestroy()
    }

    override fun onDeviceManuallyAdded(dialog: DialogFragment, device: Device) {
        discoverDeviceViewModel.insert(device)
        discoverDeviceViewModel.allDevices.value?.let {
            _binding?.deviceFoundListRecyclerView?.smoothScrollToPosition(
                it.count())
        }
    }

    override fun onDeviceDiscovered(serviceInfo: NsdServiceInfo) {
        val deviceName = serviceInfo.serviceName ?: ""
        val device = Device(serviceInfo.host.hostAddress!!, deviceName,
            isCustomName = false,
            isHidden = false,
            macAddress = ""
        )
        if (deviceListViewModel.contains(device)) {
            return
        }

        deviceListViewModel.insert(device)
        DeviceApi.update(device, false)

        activity?.runOnUiThread {
            discoverDeviceViewModel.insert(device)
            discoverDeviceViewModel.allDevices.value?.let {
                _binding?.deviceFoundListRecyclerView?.smoothScrollToPosition(
                    it.count())
            }
        }
    }
}