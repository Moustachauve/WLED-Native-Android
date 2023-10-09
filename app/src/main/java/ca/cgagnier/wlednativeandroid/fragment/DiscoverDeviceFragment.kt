package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.adapter.DeviceListFoundAdapter
import ca.cgagnier.wlednativeandroid.databinding.FragmentDiscoverDeviceBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import ca.cgagnier.wlednativeandroid.viewmodel.DiscoverDeviceViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DiscoverDeviceViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class DiscoverDeviceFragment : BottomSheetDialogFragment(),
    DeviceAddManuallyFragment.NoticeDialogListener,
    DeviceDiscovery.DeviceDiscoveredListener {

    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).deviceRepository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository)
    }
    private val discoverDeviceViewModel: DiscoverDeviceViewModel by activityViewModels {
        DiscoverDeviceViewModelFactory((requireActivity().application as DevicesApplication).deviceRepository)
    }

    private lateinit var deviceListAdapter: DeviceListFoundAdapter

    private var _binding: FragmentDiscoverDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as DevicesApplication).deviceDiscovery
            .registerDeviceDiscoveredListener(this)
        (requireActivity().application as DevicesApplication).deviceDiscovery.start()
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity().application as DevicesApplication).deviceDiscovery
            .unregisterDeviceDiscoveredListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        binding.buttonAddManually.setOnClickListener {
            val newDialog = DeviceAddManuallyFragment()
            newDialog.show(childFragmentManager, "device_add_manually")
            newDialog.registerDeviceAddedListener(this)
        }
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onPause() {
        (requireActivity().application as DevicesApplication).deviceDiscovery.stop()
        super.onPause()
    }

    override fun onResume() {
        val alertDialog = dialog as BottomSheetDialog
        alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alertDialog.behavior.skipCollapsed = true
        super.onResume()
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
            macAddress = Device.UNKNOWN_VALUE
        )
        if (deviceListViewModel.contains(device)) {
            return
        }

        deviceListViewModel.insert(device)
        DeviceApiService.update(device, false)

        activity?.runOnUiThread {
            discoverDeviceViewModel.insert(device)
            discoverDeviceViewModel.allDevices.value?.let {
                _binding?.deviceFoundListRecyclerView?.smoothScrollToPosition(
                    it.count())
            }
        }
    }
}