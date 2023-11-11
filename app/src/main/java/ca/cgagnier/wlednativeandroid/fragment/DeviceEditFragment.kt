package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceEditBinding
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceEditFragment : WiderDialogFragment() {
    private var firstLoad = true
    private lateinit var deviceAddress: String
    private lateinit var device: Device

    private var _binding: FragmentDeviceEditBinding? = null
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDeviceEditBinding.inflate(layoutInflater, null, false)

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        val branch = when(binding.branchToggleButtonGroup.checkedButtonId) {
            R.id.branch_stable_button -> Branch.STABLE
            R.id.branch_beta_button -> Branch.BETA
            else -> Branch.STABLE
        }

        val updatedDevice = device.copy(
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden,
            branch = branch
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
        binding.branchToggleButtonGroup.check(when (device.branch) {
            Branch.BETA -> R.id.branch_beta_button
            else -> R.id.branch_stable_button
        })
        binding.labelCurrentVersion.text = getString(R.string.version_v_num, device.version)

        if (!firstLoad) {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        }

        binding.buttonCheckForUpdate.visibility =
            if (device.hasUpdateAvailable()) View.GONE else View.VISIBLE
        binding.labelIsUpToDate.visibility = binding.buttonCheckForUpdate.visibility
        binding.labelCurrentVersion.visibility = binding.buttonCheckForUpdate.visibility
            binding.progressCheckForUpdate.visibility = View.GONE
        binding.buttonCheckForUpdate.isEnabled = true
        // Don't update the text if the view is not visible otherwise the transition looks very weird
        if (binding.buttonCheckForUpdate.visibility == View.VISIBLE) {
            binding.buttonCheckForUpdate.text = getString(R.string.check_for_update)
        }

        binding.iconUpdate.visibility =
            if (device.hasUpdateAvailable()) View.VISIBLE else View.GONE
        binding.updateDetails.visibility = binding.iconUpdate.visibility
        binding.buttonUpdate.visibility = binding.iconUpdate.visibility

        if (device.hasUpdateAvailable()) {
            binding.versionFromTo.text = getString(
                R.string.from_version_to_version,
                "v${device.version}",
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
        private const val IS_LARGE_LAYOUT = "is_large_layout"

        @JvmStatic
        fun newInstance(deviceAddress: String, isLargeLayout: Boolean) =
            DeviceEditFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                    putBoolean(IS_LARGE_LAYOUT, isLargeLayout)
                }
            }
    }
}