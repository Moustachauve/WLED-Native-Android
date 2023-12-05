package ca.cgagnier.wlednativeandroid.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
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
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceEditViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceEditViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DeviceEditFragment : Fragment() {
    private val deviceEditViewModel: DeviceEditViewModel by activityViewModels {
        DeviceEditViewModelFactory()
    }
    private val deviceRepository: DeviceRepository by lazy {
        (requireActivity().application as DevicesApplication).deviceRepository
    }
    private val versionWithAssetsRepository: VersionWithAssetsRepository by lazy {
        (requireActivity().application as DevicesApplication).versionWithAssetsRepository
    }

    private var firstLoad = true
    private lateinit var deviceAddress: String

    private var _binding: FragmentDeviceEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceEditBinding.inflate(layoutInflater, null, false)
        setMenu(binding.deviceToolbar)

        setFragmentResultListener(UPDATE_COMPLETED) { _, _ ->
            val intent = Intent()
            intent.putExtra(DeviceViewFragment.DEVICE_SHOULD_RELOAD, true)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }

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

        if (deviceEditViewModel.isDeviceSet()) {
            updateFields()
        } else {
            loadDevice()
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateViewModel()
        super.onSaveInstanceState(outState)
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

    private fun updateViewModel() {
        deviceEditViewModel.customName = binding.customNameTextInputLayout.editText?.text.toString()
        deviceEditViewModel.hideDevice = binding.hideDeviceCheckBox.isChecked
        deviceEditViewModel.updateBranch =
            deviceEditViewModel.getBranchFromViewId(binding.branchToggleButtonGroup.checkedButtonId)
    }

    private fun submitClickListener() {
        updateViewModel()
        val isCustomName = deviceEditViewModel.isCustomName()

        // Set the deviceName to the previous one if it's not a custom name and it wasn't a custom
        // name before the edit, otherwise the name will be lost until the next update.
        var customNameToSave = deviceEditViewModel.customName
        if (!isCustomName && !deviceEditViewModel.device.isCustomName) {
            customNameToSave = deviceEditViewModel.device.name
        }

        val languageChanged = deviceEditViewModel.branchHasChanged()
        val updatedDevice = deviceEditViewModel.device.copy(
            name = customNameToSave,
            isCustomName = isCustomName,
            isHidden = deviceEditViewModel.hideDevice,
            branch = deviceEditViewModel.updateBranch,
        )

        lifecycleScope.launch {
            Log.d(TAG, "Saving update from edit page")
            deviceRepository.update(updatedDevice)
            DeviceApiService.update(updatedDevice, false)

            if (languageChanged) {
                startDeviceUpdate()
            } else {
                requireActivity().finish()
            }
        }
    }

    private fun loadDevice() {
        deviceRepository.findLiveDeviceByAddress(deviceAddress).asLiveData()
            .observe(viewLifecycleOwner) {
                if (it != null) {
                    deviceEditViewModel.setDeviceVariables(it)
                    updateFields()
                }
            }
    }

    private fun updateFields() {
        if (firstLoad) {
            binding.deviceToolbar.title =
                getString(R.string.edit_device_with_name, deviceEditViewModel.device.name)

            binding.deviceAddressTextInputLayout.isEnabled = false
            binding.deviceAddressTextInputLayout.editText?.setText(deviceEditViewModel.device.address)
            binding.customNameTextInputLayout.editText?.setText(deviceEditViewModel.customName)
            binding.customNameTextInputLayout.requestFocus()
            binding.hideDeviceCheckBox.isChecked = deviceEditViewModel.hideDevice
            binding.branchToggleButtonGroup.check(deviceEditViewModel.getViewIdForCurrentBranch())
        }
        updateUpdateCardVisibility()
        updateUpdateFields()


        firstLoad = false
    }

    private fun updateUpdateFields() {
        val deltaSinceUpdateStart =
            System.currentTimeMillis() - deviceEditViewModel.updateCheckStartTime
        // If the check for update was really really fast, make it feel slower for a better UX.
        // It's strange, but if it's too fast, it feels like the button did nothing and people might
        // spam it.
        if (binding.progressCheckForUpdate.visibility == View.VISIBLE && deltaSinceUpdateStart < MIN_UPDATE_CHECK_TIME) {
            Log.i(TAG, "Delaying update UI refresh.")
            Handler(Looper.getMainLooper()).postDelayed({
                updateUpdateFields()
            }, MIN_UPDATE_CHECK_TIME)
            return
        }
        // deviceEditViewModel.updateCheckStartTime
        binding.labelCurrentVersion.text =
            getString(R.string.version_v_num, deviceEditViewModel.device.version)
        if (!firstLoad) {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        }

        binding.buttonCheckForUpdate.visibility =
            if (deviceEditViewModel.device.hasUpdateAvailable()) View.GONE else View.VISIBLE
        binding.labelIsUpToDate.visibility = binding.buttonCheckForUpdate.visibility
        binding.labelCurrentVersion.visibility =
            if (deviceEditViewModel.device.version != Device.UNKNOWN_VALUE) binding.buttonCheckForUpdate.visibility else View.GONE
        binding.progressCheckForUpdate.visibility = View.GONE
        binding.buttonCheckForUpdate.isEnabled = true
        // Don't update the text if the view is not visible otherwise the transition looks very weird
        if (binding.buttonCheckForUpdate.visibility == View.VISIBLE) {
            binding.buttonCheckForUpdate.text = getString(R.string.check_for_update)
        }

        binding.iconUpdate.visibility =
            if (deviceEditViewModel.device.hasUpdateAvailable()) View.VISIBLE else View.GONE
        binding.updateDetails.visibility = binding.iconUpdate.visibility
        binding.buttonUpdate.visibility = binding.iconUpdate.visibility

        if (deviceEditViewModel.device.hasUpdateAvailable()) {
            binding.versionFromTo.text = getString(
                R.string.from_version_to_version,
                "v${deviceEditViewModel.device.version}",
                deviceEditViewModel.device.newUpdateVersionTagAvailable
            )
        }
    }

    private fun updateUpdateCardVisibility() {
        binding.cardUpdateDetails.visibility =
            if (deviceEditViewModel.branchHasChanged() && deviceEditViewModel.device.branch != Branch.UNKNOWN) View.GONE else View.VISIBLE
        binding.labelSaveForUpdates.visibility =
            if (deviceEditViewModel.branchHasChanged()) View.VISIBLE else View.GONE
    }

    private fun checkForUpdate() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        binding.buttonCheckForUpdate.isEnabled = false
        binding.buttonCheckForUpdate.text = getString(R.string.checking_progress_update)
        binding.progressCheckForUpdate.visibility = View.VISIBLE
        deviceEditViewModel.updateCheckStartTime = System.currentTimeMillis()
        removeSkipVersionTag()
        refreshUpdates()
    }

    private fun removeSkipVersionTag() {
        deviceEditViewModel.device = deviceEditViewModel.device.copy(
            skipUpdateTag = ""
        )
        lifecycleScope.launch {
            Log.d(TAG, "Saving skipUpdateTag")
            deviceRepository.update(deviceEditViewModel.device)
        }
    }

    private fun refreshUpdates() {
        Log.e(TAG, "refreshing updates for device")
        val releaseService = ReleaseService(versionWithAssetsRepository)
        lifecycleScope.launch(Dispatchers.IO) {
            releaseService.refreshVersions(requireContext())
            DeviceApiService.update(deviceEditViewModel.device, false)
        }
    }

    private fun showUpdateDialog() {
        val fragmentManager = requireActivity().supportFragmentManager
        val isLargeLayout = resources.getBoolean(R.bool.large_layout)
        val newFragment = DeviceUpdateAvailableFragment.newInstance(deviceAddress, isLargeLayout)
        newFragment.show(fragmentManager, "dialog")
    }

    private fun startDeviceUpdate() {
        val updateFragment = DeviceUpdateInstallingFragment.newInstance(
            deviceAddress,
            "v${deviceEditViewModel.device.version}"
        )
        updateFragment.show(parentFragmentManager, "dialog")
    }

    companion object {
        private const val TAG = "DeviceEditFragment"
        private const val DEVICE_ADDRESS = "device_address"

        const val UPDATE_COMPLETED = "update_completed"

        private const val MIN_UPDATE_CHECK_TIME = 2000L

        @JvmStatic
        fun newInstance(deviceAddress: String) = DeviceEditFragment().apply {
            arguments = Bundle().apply {
                putString(DEVICE_ADDRESS, deviceAddress)
            }
        }
    }
}