package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceAddBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DeviceAddManuallyFragment : DialogFragment() {
    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).deviceRepository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository)
    }
    private var _binding: FragmentDeviceAddBinding? = null
    private val binding get() = _binding!!


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDeviceAddBinding.inflate(layoutInflater, null, false)

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.deviceAddressTextInputLayout.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.deviceAddressTextInputLayout, InputMethodManager.SHOW_IMPLICIT)

        binding.buttonSave.setOnClickListener {
            submitClickListener()
        }
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onResume() {
        val alertDialog = dialog
        alertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        super.onResume()
    }

    private fun submitClickListener() {
        if (!validateForm()) {
            return
        }

        val deviceAddress = binding.deviceAddressTextInputLayout.editText?.text.toString()
        val deviceName = binding.customNameTextInputLayout.editText?.text.toString()
        val isHidden = binding.hideDeviceCheckBox.isChecked

        val device = Device(
            address = deviceAddress,
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden,
            macAddress = Device.UNKNOWN_VALUE
        )

        lifecycleScope.launch(Dispatchers.IO)  {
            deviceListViewModel.insert(device)
            DeviceApiService.fromApplication(requireActivity().application as DevicesApplication)
                .refresh(device, false)
        }
        dismiss()
    }

    private fun validateForm(): Boolean {
        binding.deviceAddressTextInputLayout.error = ""
        binding.customNameTextInputLayout.error = ""

        val deviceAddress = binding.deviceAddressTextInputLayout.editText?.text.toString()

        if (deviceAddress == "") {
            binding.deviceAddressTextInputLayout.error = getString(R.string.please_enter_value)
            return false
        }
        if (deviceAddress.contains(' ')) {
            binding.deviceAddressTextInputLayout.error = getString(R.string.please_enter_valid_value)
            return false
        }

        return true
    }
}