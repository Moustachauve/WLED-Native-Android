package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceAddEditBinding
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class DeviceAddManuallyFragment : BottomSheetDialogFragment() {
    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).deviceRepository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository)
    }
    private var _binding: FragmentDeviceAddEditBinding? = null
    private val binding get() = _binding!!

    private var listeners = ArrayList<NoticeDialogListener>()

    interface NoticeDialogListener {
        fun onDeviceManuallyAdded(dialog: DialogFragment, device: Device)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceAddEditBinding.inflate(inflater, container, false)

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
        val alertDialog = dialog as BottomSheetDialog
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alertDialog.behavior.skipCollapsed = true
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

        deviceListViewModel.insert(device)
        DeviceApi.update(device, false)

        notifyListeners(device)
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

    fun registerDeviceAddedListener(listener: NoticeDialogListener) {
        listeners.add(listener)
    }

    private fun notifyListeners(device: Device) {
        for (listener in listeners) {
            listener.onDeviceManuallyAdded(this, device)
        }
    }
}