package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceAddEditBinding
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeviceEditFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDeviceAddEditBinding? = null
    private val binding get() = _binding!!

    private val manageDevicesViewModel: ManageDevicesViewModel by activityViewModels {
        ManageDevicesViewModelFactory((requireActivity().application as DevicesApplication).deviceRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceAddEditBinding.inflate(layoutInflater)

        val device = manageDevicesViewModel.activeDevice.value!!

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

        val device = manageDevicesViewModel.activeDevice.value!!.copy(
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden
        )

        manageDevicesViewModel.insert(device)
        DeviceApi.update(device, false)

        dismiss()
    }
}