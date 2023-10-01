package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceUpdateAvailableBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import kotlinx.coroutines.launch

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val DEVICE_ADDRESS = "device_address"

/**
 * A simple [Fragment] subclass.
 * Use the [DeviceUpdateAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeviceUpdateAvailableFragment : DialogFragment() {
    private var deviceAddress: String? = null
    private var device: Device? = null
    private var version: VersionWithAssets? = null
    private var _binding: FragmentDeviceUpdateAvailableBinding? = null
    private val binding get() = _binding!!
    private var isLargeLayout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)
            loadDeviceAndVersion()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceUpdateAvailableBinding.inflate(inflater, container, false)
        isLargeLayout = resources.getBoolean(R.bool.large_layout)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        return dialog
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            windowInsets
        }
        binding.mainToolbar.setNavigationOnClickListener {
            if (isLargeLayout) {
                dismiss()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun loadDeviceAndVersion() {
        val deviceRepository =
            (requireActivity().application as DevicesApplication).deviceRepository
        val versionRepository =
            (requireActivity().application as DevicesApplication).versionWithAssetsRepository
        lifecycleScope.launch {
            device = deviceRepository.findDeviceByAddress(deviceAddress!!)
            version = versionRepository.getLatestVersionWithAssets()
            updateFields()
        }
    }

    private fun updateFields() {
        binding.deviceName.text = device!!.name
        binding.deviceAddress.text = device!!.address
        binding.versionTag.text = version!!.version.tagName
        binding.versionNotes.text = version!!.version.description
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param deviceAddress Address of device that can be updated
         * @return A new instance of fragment device_update_available.
         */
        @JvmStatic
        fun newInstance(deviceAddress: String) =
            DeviceUpdateAvailableFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                }
            }
    }
}