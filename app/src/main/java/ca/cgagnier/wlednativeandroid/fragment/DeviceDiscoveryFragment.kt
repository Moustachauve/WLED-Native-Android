package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.adapter.DeviceListFoundAdapter
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceDiscoveryBinding
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DeviceViewModel
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DeviceDiscoveryFragment : DialogFragment(),
    DeviceAddManuallyFragment.NoticeDialogListener,
    DeviceDiscovery.DeviceDiscoveredListener,
    DeviceRepository.DataChangedListener {

    private lateinit var deviceDiscovery: DeviceDiscovery
    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private lateinit var deviceListAdapter: DeviceListFoundAdapter

    private var _binding: FragmentDeviceDiscoveryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceRepository.registerDataChangedListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deviceDiscovery = DeviceDiscovery(context)
        deviceDiscovery.registerDeviceDiscoveredListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        deviceDiscovery.unregisterDeviceDiscoveredListener(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDeviceDiscoveryBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(binding.root.context)

        deviceListAdapter = DeviceListFoundAdapter(ArrayList()) {deviceItem: DeviceItem, _: Int ->
            deviceViewModel.updateCurrentDevice(deviceItem)
            deviceViewModel.updateSelectedIndex(DeviceRepository.getPositionOfDevice(deviceItem))
            dismiss()
        }

        binding.deviceFoundListRecyclerView.adapter = deviceListAdapter
        binding.deviceFoundListRecyclerView.layoutManager = layoutManager
        binding.deviceFoundListRecyclerView.setHasFixedSize(false)

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder
            .setPositiveButton(getString(R.string.add_device_manually), null)
            .setNeutralButton(R.string.close, null)
            .setView(binding.root)
        return builder.create()
    }

    override fun onPause() {
        deviceDiscovery.stop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        deviceDiscovery.start()

        val alertDialog = dialog as AlertDialog

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newDialog = DeviceAddManuallyFragment()
            newDialog.showsDialog = true
            newDialog.show(childFragmentManager, "device_add_manually")
            newDialog.registerDeviceAddedListener(this)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        deviceDiscovery.stop()
        super.onDestroy()
        DeviceRepository.unregisterDataChangedListener(this)
    }

    override fun onDeviceManuallyAdded(dialog: DialogFragment, device: DeviceItem) {
        val newIndex = deviceListAdapter.addItem(device)
        _binding?.deviceFoundListRecyclerView?.smoothScrollToPosition(newIndex)
    }

    override fun onDeviceDiscovered(serviceInfo: NsdServiceInfo) {

        val deviceName = serviceInfo.serviceName ?: ""
        val device = DeviceItem(serviceInfo.host.hostAddress!!, deviceName)
        if (DeviceRepository.contains(device)) {
            return
        }

        DeviceRepository.put(device)
        DeviceApi.update(device)

        activity?.runOnUiThread {
            val newIndex = deviceListAdapter.addItem(device)
            _binding?.deviceFoundListRecyclerView?.smoothScrollToPosition(newIndex)
        }
    }

    override fun onItemChanged(item: DeviceItem) {
        deviceListAdapter.itemChanged(item)
    }

    override fun onItemAdded(item: DeviceItem) {
    }

    override fun onItemRemoved(item: DeviceItem) {
    }
}