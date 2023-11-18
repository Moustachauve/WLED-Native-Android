package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DeviceEditFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceEditBinding.inflate(layoutInflater, null, false)

        setMenu(binding.deviceToolbar)
        binding.buttonCheckForUpdate.setOnClickListener {
            checkForUpdate()
        }
        binding.buttonUpdate.setOnClickListener {
            showUpdateDialog()
        }
        binding.branchToggleButtonGroup.addOnButtonCheckedListener { _, _, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }
            updateUpdateCardVisibility()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDevice()
    }

    private fun setMenu(toolbar: MaterialToolbar) {
        binding.deviceToolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.device_edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_device_save -> {
                        submitClickListener()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun getBranchFromButton(): Branch {
        return when (binding.branchToggleButtonGroup.checkedButtonId) {
            R.id.branch_beta_button -> Branch.BETA
            else -> Branch.STABLE
        }
    }

    private fun submitClickListener() {
        var deviceName = binding.customNameTextInputLayout.editText?.text.toString()
        val isHidden = binding.hideDeviceCheckBox.isChecked
        val branch = getBranchFromButton()
        val branchChanged = device.branch != branch
        val isCustomName = deviceName != ""
        // If the branch changed, reset the next update available so it can be checked again
        val nextUpdateTag = if (branchChanged) "" else device.newUpdateVersionTagAvailable
        // Set the deviceName to the previous one if it's not a custom name and it wasn't a custom
        // name before the edit, otherwise the name will be lost until the next update.
        if (!isCustomName && !device.isCustomName) {
            deviceName = device.name
        }

        val updatedDevice = device.copy(
            name = deviceName,
            isCustomName = isCustomName,
            isHidden = isHidden,
            branch = branch,
            newUpdateVersionTagAvailable = nextUpdateTag
        )

        lifecycleScope.launch {
            Log.d(TAG, "Saving update from edit page")
            deviceRepository.update(updatedDevice)
            DeviceApiService.update(updatedDevice, false)
            requireActivity().finish()
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
        if (firstLoad) {
            binding.deviceToolbar.title = getString(R.string.edit_device_with_name, device.name)

            binding.deviceAddressTextInputLayout.isEnabled = false
            binding.deviceAddressTextInputLayout.editText?.setText(device.address)
            binding.customNameTextInputLayout.editText?.setText(if (device.isCustomName) device.name else "")
            binding.customNameTextInputLayout.requestFocus()
            binding.hideDeviceCheckBox.isChecked = device.isHidden
            binding.branchToggleButtonGroup.check(
                when (device.branch) {
                    Branch.BETA -> R.id.branch_beta_button
                    else -> R.id.branch_stable_button
                }
            )
        }
        updateUpdateCardVisibility()

        binding.labelCurrentVersion.text = getString(R.string.version_v_num, device.version)
        if (!firstLoad) {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        }

        binding.buttonCheckForUpdate.visibility =
            if (device.hasUpdateAvailable()) View.GONE else View.VISIBLE
        binding.labelIsUpToDate.visibility = binding.buttonCheckForUpdate.visibility
        binding.labelCurrentVersion.visibility = if (device.version != Device.UNKNOWN_VALUE) binding.buttonCheckForUpdate.visibility else View.GONE
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

    private fun updateUpdateCardVisibility() {
        binding.cardUpdateDetails.visibility =
            if (device.branch == getBranchFromButton() || device.branch == Branch.UNKNOWN) View.VISIBLE else View.GONE
        binding.labelSaveForUpdates.visibility =
            if (device.branch == getBranchFromButton()) View.GONE else View.VISIBLE
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
        Log.e(TAG, "refreshing updates for device")
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

        @JvmStatic
        fun newInstance(deviceAddress: String) =
            DeviceEditFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                }
            }
    }
}