package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout


class DeviceAddManuallyFragment : DialogFragment() {
    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).repository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository)
    }

    private var listeners = ArrayList<NoticeDialogListener>()

    lateinit var deviceAddressTextInputLayout: TextInputLayout
    lateinit var customNameTextTextInputLayout: TextInputLayout
    lateinit var hideDeviceCheckBox: CheckBox

    interface NoticeDialogListener {
        fun onDeviceManuallyAdded(dialog: DialogFragment, device: Device)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setMessage(R.string.add_device_manually)
            .setPositiveButton(R.string.add_device, null)
            .setNegativeButton(R.string.cancel, null)
            .setView(R.layout.fragment_device_add_edit)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        val alertDialog = dialog as AlertDialog

        deviceAddressTextInputLayout = alertDialog.findViewById(R.id.device_address_text_input_layout)!!
        customNameTextTextInputLayout = alertDialog.findViewById(R.id.custom_name_text_input_layout)!!
        hideDeviceCheckBox = alertDialog.findViewById(R.id.hide_device_check_box)!!

        deviceAddressTextInputLayout.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(deviceAddressTextInputLayout, InputMethodManager.SHOW_IMPLICIT)

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            submitClickListener()
        }
    }

    private fun submitClickListener() {
        if (!validateForm()) {
            return
        }

        val deviceAddress = deviceAddressTextInputLayout.editText?.text.toString()
        val deviceName = customNameTextTextInputLayout.editText?.text.toString()
        val isHidden = hideDeviceCheckBox.isChecked

        val device = Device(
            address = deviceAddress,
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden
        )

        deviceListViewModel.insert(device)
        DeviceApi.update(device, false)

        notifyListeners(device)
        dismiss()
    }

    private fun validateForm(): Boolean {
        deviceAddressTextInputLayout.error = ""
        customNameTextTextInputLayout.error = ""

        val deviceAddress = deviceAddressTextInputLayout.editText?.text.toString()

        if (deviceAddress == "") {
            deviceAddressTextInputLayout.error = getString(R.string.please_enter_value)
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