package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceUpdateDisclaimerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


private const val DEVICE_ADDRESS = "device_address"
private const val VERSION_TAG = "version_tag"

class DeviceUpdateDisclaimerFragment : DialogFragment() {

    private lateinit var deviceAddress: String
    private lateinit var versionTag: String

    private var _binding: FragmentDeviceUpdateDisclaimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
            versionTag = it.getString(VERSION_TAG)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDeviceUpdateDisclaimerBinding.inflate(layoutInflater, null, false)

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
        binding.buttonContinue.setOnClickListener {
            dismiss()
            DeviceUpdateInstallingFragment.newInstance(
                deviceAddress,
                versionTag
            ).show(parentFragmentManager, "dialog")
        }
        return binding.root
    }

    companion object {
        const val TAG = "DeviceUpdateDisclaimerFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param deviceAddress Address of device that can be updated
         * @return A new instance of fragment device_update_disclaimer.
         */
        @JvmStatic
        fun newInstance(deviceAddress: String, versionTag: String) =
            DeviceUpdateDisclaimerFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                    putString(VERSION_TAG, versionTag)
                }
            }
    }
}