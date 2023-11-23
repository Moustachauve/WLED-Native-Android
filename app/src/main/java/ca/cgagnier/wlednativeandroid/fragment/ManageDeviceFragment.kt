package ca.cgagnier.wlednativeandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ca.cgagnier.wlednativeandroid.DeviceEditActivity
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.adapter.DeviceListManageAdapter
import ca.cgagnier.wlednativeandroid.databinding.FragmentManageDevicesBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ManageDeviceFragment : BottomSheetDialogFragment() {

    private val manageDevicesViewModel: ManageDevicesViewModel by activityViewModels {
        ManageDevicesViewModelFactory((requireActivity().application as DevicesApplication).deviceRepository)
    }

    private lateinit var deviceListAdapter: DeviceListManageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentManageDevicesBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(binding.root.context)

        deviceListAdapter = DeviceListManageAdapter(
            onItemClicked = { device: Device ->
                Log.i(TAG, "Requesting to open the device view")
                setFragmentResult(
                    DeviceListFragment.REQUEST_OPEN_DEVICE_KEY,
                    DeviceListFragment.createOpenDeviceBundle(device.address)
                )
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

        return binding.root
    }

    override fun onResume() {
        val alertDialog = dialog as BottomSheetDialog
        alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alertDialog.behavior.skipCollapsed = true
        super.onResume()
    }

    private fun editItem(device: Device) {
        val intent = Intent(requireActivity(), DeviceEditActivity::class.java)
        intent.putExtra(DeviceEditActivity.EXTRA_DEVICE_ADDRESS, device.address)
        startActivity(intent)
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

    companion object {
        private const val TAG = "ManageDeviceFragment"
    }
}