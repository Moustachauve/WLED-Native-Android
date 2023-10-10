package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceAddEditBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceEditFragment : DialogFragment() {
    private var firstLoad = true
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

    private fun submitClickListener() {
        val deviceName = binding.customNameTextInputLayout.editText?.text.toString()
        val isHidden = binding.hideDeviceCheckBox.isChecked

        val updatedDevice = device.copy(
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden
        )

        lifecycleScope.launch {
            Log.d(TAG, "Saving update from edit page")
            deviceRepository.update(updatedDevice)
            DeviceApiService.update(updatedDevice, false)
            dismiss()
        }
    }

    private fun loadDevice() {
        deviceRepository.findLiveDeviceByAddress(deviceAddress).asLiveData()
            .observe(viewLifecycleOwner) {
                if (it != null) {
                    device = it
                    updateFields()
                }
            }
    }

    private fun updateFields() {
        binding.dialogTitle.text = getString(R.string.edit_device_with_name, device.name)

        binding.deviceAddressTextInputLayout.isEnabled = false
        binding.deviceAddressTextInputLayout.editText?.setText(device.address)
        binding.customNameTextInputLayout.editText?.setText(if (device.isCustomName) device.name else "")
        binding.customNameTextInputLayout.requestFocus()
        binding.hideDeviceCheckBox.isChecked = device.isHidden

        if (!firstLoad) {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        }

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
            binding.versionFromTo.text = getString(
                R.string.from_version_to_version,
                "v$device.version",
                device.newUpdateVersionTagAvailable
            )
        }

        firstLoad = false
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
            Log.d(TAG, "Saving skipUpdateTag")
            deviceRepository.update(device)
        }
    }

    private fun refreshUpdates() {
        val releaseService = ReleaseService(versionWithAssetsRepository)
        lifecycleScope.launch(Dispatchers.IO) {
            releaseService.refreshVersions(requireContext())
            DeviceApiService.update(device, false)
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
        private const val TAG = "DeviceEditFragment"
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