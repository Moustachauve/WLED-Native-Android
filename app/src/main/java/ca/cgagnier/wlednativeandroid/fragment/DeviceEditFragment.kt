package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceAddEditBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class DeviceEditFragment : BottomSheetDialogFragment() {
    private lateinit var deviceAddress: String
    private lateinit var device: Device

    private var _binding: FragmentDeviceAddEditBinding? = null
    private val binding get() = _binding!!

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
        return binding.root
    }

    override fun onResume() {
        val alertDialog = dialog as BottomSheetDialog
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alertDialog.behavior.skipCollapsed = true
        super.onResume()
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
            (requireActivity().application as DevicesApplication).deviceRepository.insert(updatedDevice)
            DeviceApi.update(updatedDevice, false)
            dismiss()
        }
    }

    private fun loadDevice() {
        val deviceRepository =
            (requireActivity().application as DevicesApplication).deviceRepository
        lifecycleScope.launch {
            device = deviceRepository.findDeviceByAddress(deviceAddress)!!
            updateFields()
        }
    }

    private fun updateFields() {
        binding.dialogTitle.text = context?.getString(R.string.edit_device)

        binding.deviceAddressTextInputLayout.isEnabled = false
        binding.deviceAddressTextInputLayout.editText?.setText(device.address)
        binding.customNameTextInputLayout.editText?.setText(if (device.isCustomName) device.name else "")
        binding.customNameTextInputLayout.requestFocus()
        binding.hideDeviceCheckBox.isChecked = device.isHidden

        binding.buttonSave.setOnClickListener {
            submitClickListener()
        }
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
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