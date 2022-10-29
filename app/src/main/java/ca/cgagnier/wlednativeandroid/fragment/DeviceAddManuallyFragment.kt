package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class DeviceAddManuallyFragment : DialogFragment() {
    private var listeners = ArrayList<NoticeDialogListener>()

    lateinit var deviceAddressTextInputLayout: TextInputLayout
    lateinit var customNameTextTextInputLayout: TextInputLayout
    lateinit var hideDeviceCheckBox: CheckBox

    interface NoticeDialogListener {
        fun onDeviceManuallyAdded(dialog: DialogFragment, device: DeviceItem)
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

        val device = DeviceItem(
            address = deviceAddress,
            name = deviceName,
            isCustomName = deviceName != "",
            isHidden = isHidden
        )

        DeviceRepository.put(device)
        DeviceApi.update(device)

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

    fun unregisterDeviceAddedListener(listener: NoticeDialogListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(device: DeviceItem) {
        for (listener in listeners) {
            listener.onDeviceManuallyAdded(this, device)
        }
    }
}