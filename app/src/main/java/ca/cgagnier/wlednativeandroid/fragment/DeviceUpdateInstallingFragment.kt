package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceUpdateInstallingBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.update.DeviceUpdateService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val DEVICE_ADDRESS = "device_address"
private const val VERSION_TAG = "version_tag"

/**
 * A simple [Fragment] subclass.
 * Use the [DeviceUpdateAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeviceUpdateInstallingFragment : DialogFragment() {
    private lateinit var deviceAddress: String
    private lateinit var device: Device
    private lateinit var versionTag: String
    private lateinit var version: VersionWithAssets

    private var _binding: FragmentDeviceUpdateInstallingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
            versionTag = it.getString(VERSION_TAG)!!
            loadDeviceAndVersion()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDeviceUpdateInstallingBinding.inflate(layoutInflater, null, false)

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun loadDeviceAndVersion() {
        val deviceRepository =
            (requireActivity().application as DevicesApplication).deviceRepository
        val versionRepository =
            (requireActivity().application as DevicesApplication).versionWithAssetsRepository
        lifecycleScope.launch {
            device = deviceRepository.findDeviceByAddress(deviceAddress)!!
            version = versionRepository.getVersionByTag(versionTag)!!
            updateFields()
            startUpdate()
        }
    }

    private fun updateFields() {
        binding.textUpdating.text = getString(R.string.updating, device.name)
        binding.textVersionTag.text = version.version.tagName
    }

    private fun startUpdate() {
        binding.textStatus.text = getString(R.string.downloading_binaries)
        val updateService = DeviceUpdateService(requireContext(), device, version)
        if (!updateService.couldDetermineAsset()) {
            // TODO Handle no asset found
            return
        }
        val asset = updateService.getAsset()
        binding.textVersionTag.text = asset.name
        binding.progressUpdate.isIndeterminate = false

        lifecycleScope.launch(Dispatchers.IO) {
            updateService.downloadBinary().collect { downloadState ->
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Log.d(TAG, "progress=${downloadState.progress}")
                        activity?.runOnUiThread {
                            binding.progressUpdate.isIndeterminate = false
                            binding.progressUpdate.progress = downloadState.progress
                        }
                    }
                    is DownloadState.Failed -> {
                        Log.e(TAG, "Fail")
                        //showError()
                    }
                    is DownloadState.Finished -> {
                        Log.d(TAG, "Finished")
                        activity?.runOnUiThread {
                            binding.progressUpdate.isIndeterminate = true
                            installUpdate()
                        }
                    }
                }
            }
        }
    }

    private fun installUpdate() {
        binding.textStatus.text = getString(R.string.installing_update)
        dialog?.setCancelable(false)
        binding.buttonCancel.isEnabled = false
        // TODO: Install the update
    }

    companion object {
        const val TAG = "DeviceUpdateInstallingFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param deviceAddress Address of device that can be updated
         * @return A new instance of fragment device_update_installing.
         */
        @JvmStatic
        fun newInstance(deviceAddress: String, versionTag: String) =
            DeviceUpdateInstallingFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                    putString(VERSION_TAG, versionTag)
                }
            }
    }
}