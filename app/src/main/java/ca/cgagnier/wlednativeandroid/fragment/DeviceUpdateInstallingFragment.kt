package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceUpdateInstallingBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.SoftwareUpdateRequest
import ca.cgagnier.wlednativeandroid.service.update.DeviceUpdateService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response


private const val DEVICE_ADDRESS = "device_address"
private const val VERSION_TAG = "version_tag"


class DeviceUpdateInstallingFragment : DialogFragment() {
    private val deviceStateFactory by lazy {
        (requireActivity().application as DevicesApplication).deviceStateFactory
    }
    private lateinit var deviceAddress: String
    private lateinit var device: Device
    private lateinit var versionTag: String
    private lateinit var version: VersionWithAssets

    private var errorString = ""
    private var beforeErrorString = ""

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
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        binding.buttonErrorDetails.setOnClickListener {
            if (errorString.isEmpty()) {
                binding.buttonErrorDetails.visibility = View.GONE
                return@setOnClickListener
            }

            val currentText = (binding.textUpdatingWarning.currentView as TextView).text
            if (currentText == errorString) {
                changeDetailText(beforeErrorString)
                binding.buttonErrorDetails.text = getString(R.string.show_error)
                return@setOnClickListener
            }

            beforeErrorString = currentText.toString()
            changeDetailText(errorString)
            binding.buttonErrorDetails.text = getString(R.string.hide_error)
        }
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
        Log.d(TAG, "Starting update")
        binding.textStatus.text = getString(R.string.downloading_version)
        val updateService = DeviceUpdateService(requireContext(), device, version)
        binding.textVersionTag.text = updateService.getVersionWithPlatformName()
        if (!updateService.couldDetermineAsset()) {
            displayNoBinary()
            return
        }
        binding.progressUpdate.isIndeterminate = false

        lifecycleScope.launch(Dispatchers.IO) {
            if (updateService.isAssetFileCached()) {
                Log.d(TAG, "asset is already downloaded, reusing")
                activity?.runOnUiThread {
                    installUpdate(updateService)
                }
                return@launch
            }

            updateService.downloadBinary().collect { downloadState ->
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Log.d(TAG, "File download Progress=${downloadState.progress}")
                        activity?.runOnUiThread {
                            binding.textVersionTag.text = updateService.getAsset().name
                            binding.progressUpdate.isIndeterminate = false
                            binding.progressUpdate.progress = downloadState.progress
                        }
                    }

                    is DownloadState.Failed -> {
                        Log.e(TAG, "File download Fail: ${downloadState.error}")
                        activity?.runOnUiThread {
                            errorString = downloadState.error.toString()
                            displayFailure()
                        }
                    }

                    is DownloadState.Finished -> {
                        Log.d(TAG, "File download Finished")
                        activity?.runOnUiThread {
                            installUpdate(updateService)
                        }
                    }
                }
            }
        }
    }

    private fun installUpdate(updateService: DeviceUpdateService) {
        binding.progressUpdate.isIndeterminate = true
        binding.textStatus.text = getString(R.string.installing_update)
        dialog?.setCancelable(false)
        binding.buttonCancel.isEnabled = false

        Log.d(TAG, "Uploading binary to device")
        deviceStateFactory.getState(device).requestsManager.addRequest(SoftwareUpdateRequest(
            device,
            updateService.getPathForAsset(),
            ::onSoftwareUpdateResponse,
            ::onSoftwareUpdateError
        ))
    }

    private fun onSoftwareUpdateResponse(response: Response<ResponseBody>) {
        activity?.runOnUiThread {
            if (response.code() in 200..299) {
                displaySuccess()
            } else {
                Log.d(TAG, "OTA Failed, code ${response.code()}")
                errorString = "${response.code()}: ${getHtmlErrorMessage(response)}"
                Log.d(TAG, "OTA Failed onResponse, error $errorString")
                displayFailure(getString(R.string.ota_install_failed_device_locked))
            }
            updateDeviceUpdated()
        }
    }

    private fun onSoftwareUpdateError(e: Exception) {
        activity?.runOnUiThread {
            Log.d(TAG, "OTA Failed, call failed")
            Log.e(TAG, e.toString())
            errorString = e.toString()
            displayFailure()
            Log.d(TAG, "OTA Failed onFailure, error $errorString")
        }
    }

    private fun getHtmlErrorMessage(response: Response<ResponseBody>): String {
        val bodyHtml = Jsoup.parseBodyFragment(
            response.body()?.string() ?: response.errorBody()?.string() ?: ""
        )
        bodyHtml.select("title").remove()
        bodyHtml.select("button").remove()
        bodyHtml.select("h1").remove()
        return bodyHtml.text()
    }

    private fun displaySuccess() {
        binding.progressUpdate.visibility = View.INVISIBLE
        binding.textUpdatingWarning.visibility = View.GONE
        binding.imageUpdateSuccess.visibility = View.VISIBLE
        binding.textStatus.text = getString(R.string.update_completed)
        binding.buttonCancel.text = getString(R.string.done)
        dialog?.setCancelable(true)
        binding.buttonCancel.isEnabled = true
    }

    private fun displayFailure(errorMessage: String = "") {
        binding.progressUpdate.visibility = View.INVISIBLE
        binding.textUpdatingWarning.setText(errorMessage)
        binding.imageUpdateFailed.visibility = View.VISIBLE
        binding.textStatus.text = getString(R.string.update_failed)
        binding.buttonCancel.text = getString(R.string.done)
        dialog?.setCancelable(true)
        binding.buttonCancel.isEnabled = true

        binding.buttonErrorDetails.visibility =
            if (errorString.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun displayNoBinary() {
        displayFailure()
        binding.textStatus.text = getString(R.string.no_compatible_version_found)
        changeDetailText(getString(R.string.no_compatible_version_found_details))
    }

    private fun updateDeviceUpdated() {
        device = device.copy(
            version = version.version.tagName.drop(1),
            newUpdateVersionTagAvailable = ""
        )

        lifecycleScope.launch {
            Log.d(TAG, "Saving deviceUpdated")
            val deviceRepository =
                (requireActivity().application as DevicesApplication).deviceRepository
            deviceRepository.update(device)
            deviceStateFactory.getState(device).requestsManager.addRequest(RefreshRequest(device))
        }
    }

    private fun changeDetailText(text: String) {
        binding.textUpdatingWarning.setText(text)
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