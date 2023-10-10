package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
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
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

private const val DEVICE_ADDRESS = "device_address"
private const val IS_LARGE_LAYOUT = "is_large_layout"

/**
 * A simple [Fragment] subclass.
 * Use the [DeviceUpdateAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeviceUpdateAvailableFragment : DialogFragment() {
    private lateinit var deviceAddress: String
    private lateinit var device: Device
    private lateinit var version: VersionWithAssets
    private var _binding: FragmentDeviceUpdateAvailableBinding? = null
    private val binding get() = _binding!!
    private var isLargeLayout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
            isLargeLayout = it.getBoolean(IS_LARGE_LAYOUT)
            loadDeviceAndVersion()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceUpdateAvailableBinding.inflate(inflater, container, false)

        binding.buttons.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_install -> {
                    dismiss()
                    DeviceUpdateDisclaimerFragment.newInstance(
                        deviceAddress,
                        version.version.tagName
                    ).show(parentFragmentManager, "dialog")
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

    override fun onResume() {
        super.onResume()
        var width = WindowManager.LayoutParams.MATCH_PARENT
        var layoutType = WindowManager.LayoutParams.MATCH_PARENT
        if (isLargeLayout) {
            width = (getScreenWidth() * 0.70).toInt()
            layoutType = WindowManager.LayoutParams.WRAP_CONTENT
        }
        val window = dialog!!.window!!
        window.setLayout(width, layoutType)
        window.setGravity(Gravity.CENTER)
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
            version = versionRepository.getLatestVersionWithAssets()!!
            updateFields()
        }
    }

    private fun updateFields() {
        binding.deviceName.text = device.name
        binding.deviceAddress.text = device.address

        Markwon.create(requireContext())
            .setMarkdown(binding.versionNotes, version.version.description)
    }

    private fun getScreenWidth(): Int {
        val windowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val windowInsets: WindowInsets = windowMetrics.windowInsets

            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
            )
            val insetsWidth = insets.right + insets.left

            val b = windowMetrics.bounds
            width = b.width() - insetsWidth
        } else {
            val size = Point()
            // This branch is only to support old devices, so deprecation is fine.
            @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display?.getSize(size)
            width = size.x
        }

        return width
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
            val deviceRepository =
                (requireActivity().application as DevicesApplication).deviceRepository
            deviceRepository.update(updatedDevice)
            dismiss()
        }

    }

    companion object {
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