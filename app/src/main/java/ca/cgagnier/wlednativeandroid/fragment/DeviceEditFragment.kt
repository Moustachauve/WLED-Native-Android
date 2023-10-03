package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceAddEditBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.service.update.UpdateService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceEditFragment : BottomSheetDialogFragment() {
    private lateinit var deviceAddress: String
    private lateinit var device: Device

    private var _binding: FragmentDeviceAddEditBinding? = null
    private val binding get() = _binding!!

    private val deviceRepository: DeviceRepository by lazy {
        (requireActivity().application as DevicesApplication).deviceRepository
    }
    private val versionWithAssetsRepository: VersionWithAssetsRepository by lazy {
        (requireActivity().application as DevicesApplication).versionWithAssetsRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceAddEditBinding.inflate(layoutInflater)
        loadDevice()

        binding.buttonSave.setOnClickListener {
            submitClickListener()
        }
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonCheckForUpdate.setOnClickListener {
            checkForUpdate()
        }

        binding.buttonUpdate.setOnClickListener {
            showUpdateDialog()
        }
        return binding.root
    }

    override fun onResume() {
        val alertDialog = dialog as BottomSheetDialog
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alertDialog.behavior.skipCollapsed = true
        super.onResume()
    }

    private fun submitClickListener() {
        val deviceName = binding.customNameTextInputLayout.editText?.text.toString()
        val isHidden = binding.hideDeviceCheckBox.isChecked

        val updatedDevice = device.copy(
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden
        )

        lifecycleScope.launch {
            deviceRepository.update(updatedDevice)
            DeviceApi.update(updatedDevice, false)
            dismiss()
        }
    }

    private fun loadDevice() {
        deviceRepository.findLiveDeviceByAddress(deviceAddress)
            .observe(viewLifecycleOwner) {
                device = it
                updateFields()
            }
    }

    private fun updateFields() {
        binding.dialogTitle.text = getString(R.string.edit_device_with_name, device.name)

        binding.deviceAddressTextInputLayout.isEnabled = false
        binding.deviceAddressTextInputLayout.editText?.setText(device.address)
        binding.customNameTextInputLayout.editText?.setText(if (device.isCustomName) device.name else "")
        binding.customNameTextInputLayout.requestFocus()
        binding.hideDeviceCheckBox.isChecked = device.isHidden

        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

        binding.buttonCheckForUpdate.visibility =
            if (device.hasUpdateAvailable()) View.GONE else View.VISIBLE
        binding.progressCheckForUpdate.visibility = View.GONE
        binding.buttonCheckForUpdate.isEnabled = true
        // Don't update the text if the view is not visible otherwise the transition looks very weird
        if (binding.buttonCheckForUpdate.visibility == View.VISIBLE) {
            binding.buttonCheckForUpdate.text = getString(R.string.check_for_update)
        }

        binding.layoutUpdateAvailable.visibility =
            if (device.hasUpdateAvailable()) View.VISIBLE else View.GONE
        if (device.hasUpdateAvailable()) {
            binding.versionFromTo.text =
                getString(R.string.from_version_to_version, device.version, device.version)
        }
    }

    private fun checkForUpdate() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        binding.buttonCheckForUpdate.isEnabled = false
        binding.buttonCheckForUpdate.text = getString(R.string.checking_progress_update)
        binding.progressCheckForUpdate.visibility = View.VISIBLE
        removeSkipVersionTag()
        refreshUpdates()
    }

    private fun removeSkipVersionTag() {
        device = device.copy(
            skipUpdateTag = ""
        )
        lifecycleScope.launch {
            deviceRepository.update(device)
        }
    }

    private fun refreshUpdates() {
        val updateService = UpdateService(versionWithAssetsRepository)
        lifecycleScope.launch(Dispatchers.IO) {
            updateService.refreshVersions(requireContext())
            DeviceApi.update(device, false)
        }
    }

    private fun showUpdateDialog() {
        val fragmentManager = requireActivity().supportFragmentManager
        val isLargeLayout = resources.getBoolean(R.bool.large_layout)
        val newFragment =
            DeviceUpdateAvailableFragment.newInstance(deviceAddress, isLargeLayout)
        newFragment.show(fragmentManager, "dialog")
    }

    companion object {
        private const val DEVICE_ADDRESS = "device_address"

        /**
         * @param deviceAddress Address of device that can be updated
         * @return A new instance of DeviceEditFragment.
         */
        @JvmStatic
        fun newInstance(deviceAddress: String) =
            DeviceEditFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                }
            }
    }
}