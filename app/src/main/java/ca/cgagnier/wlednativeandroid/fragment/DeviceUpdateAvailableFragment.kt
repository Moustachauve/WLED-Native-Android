package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceUpdateAvailableBinding
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

private const val DEVICE_ADDRESS = "device_address"
private const val IS_LARGE_LAYOUT = "is_large_layout"

/**
 * A simple [Fragment] subclass.
 * Use the [DeviceUpdateAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeviceUpdateAvailableFragment : WiderDialogFragment() {
    private lateinit var deviceAddress: String
    private lateinit var device: Device
    private lateinit var version: VersionWithAssets
    private var _binding: FragmentDeviceUpdateAvailableBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
            loadDeviceAndVersion()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDeviceUpdateAvailableBinding.inflate(layoutInflater, null, false)

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.buttons.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_install -> {
                    dismiss()
                    startUpdateInstall()
                    return@setOnItemSelectedListener true
                }

                R.id.action_later -> {
                    dismiss()
                    return@setOnItemSelectedListener true
                }

                R.id.action_skip -> {
                    skipVersion()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayoutDeviceUpdate) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            windowInsets
        }
        binding.mainToolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun loadDeviceAndVersion() {
        val deviceRepository =
            (requireActivity().application as DevicesApplication).deviceRepository
        val versionRepository =
            (requireActivity().application as DevicesApplication).versionWithAssetsRepository
        lifecycleScope.launch {
            device = deviceRepository.findDeviceByAddress(deviceAddress)!!
            version = if (device.branch == Branch.BETA) {
                versionRepository.getLatestBetaVersionWithAssets()!!
            } else {
                versionRepository.getLatestStableVersionWithAssets()!!
            }
            updateFields()
        }
    }

    private fun updateFields() {
        binding.deviceName.text = device.name
        binding.deviceAddress.text = device.address

        if (!device.isOnline) {
            val installButton = binding.buttons.menu.findItem(R.id.action_install)
            installButton.title = getString(R.string.device_offline)
            installButton.icon = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.twotone_signal_wifi_connected_no_internet_0_24
            )
            installButton.isEnabled = false
        }

        Markwon.create(requireContext())
            .setMarkdown(binding.versionNotes, version.version.description)
    }

    private fun skipVersion() {
        val updatedDevice = device.copy(
            newUpdateVersionTagAvailable = "",
            skipUpdateTag = version.version.tagName
        )
        if (updatedDevice == device) {
            dismiss()
            return
        }

        lifecycleScope.launch {
            Log.d(TAG, "Saving skipUpdateTag from update available dialog")
            val deviceRepository =
                (requireActivity().application as DevicesApplication).deviceRepository
            deviceRepository.update(updatedDevice)
            dismiss()
        }
    }

    private fun startUpdateInstall() {
        DeviceUpdateInstallingFragment.newInstance(
            deviceAddress,
            version.version.tagName
        ).show(parentFragmentManager, "dialog")
    }

    companion object {
        private const val TAG = "DeviceUpdateAvailableFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param deviceAddress Address of device that can be updated
         * @param isLargeLayout True if the fragment is displayed originally on a large display
         * @return A new instance of fragment device_update_available.
         */
        @JvmStatic
        fun newInstance(deviceAddress: String, isLargeLayout: Boolean) =
            DeviceUpdateAvailableFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                    putBoolean(IS_LARGE_LAYOUT, isLargeLayout)
                }
            }
    }
}