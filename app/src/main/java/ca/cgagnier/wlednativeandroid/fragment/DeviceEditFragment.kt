package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.ManageDevicesViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class DeviceEditFragment : DialogFragment() {

    private val manageDevicesViewModel: ManageDevicesViewModel by activityViewModels {
        ManageDevicesViewModelFactory((requireActivity().application as DevicesApplication).repository)
    }

    lateinit var deviceAddressTextInputLayout: TextInputLayout
    lateinit var customNameTextTextInputLayout: TextInputLayout
    lateinit var hideDeviceCheckBox: CheckBox

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setMessage(R.string.add_device_manually)
                .setPositiveButton(getString(R.string.save_device), null)
                .setNegativeButton(R.string.cancel, null)
                .setView(R.layout.fragment_device_add_edit)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        val device = manageDevicesViewModel.activeDevice.value!!

        deviceAddressTextInputLayout = alertDialog.findViewById(R.id.device_address_text_input_layout)!!
        customNameTextTextInputLayout = alertDialog.findViewById(R.id.custom_name_text_input_layout)!!
        hideDeviceCheckBox = alertDialog.findViewById(R.id.hide_device_check_box)!!

        deviceAddressTextInputLayout.isEnabled = false
        deviceAddressTextInputLayout.editText?.setText(device.address)
        customNameTextTextInputLayout.editText?.setText(if (device.isCustomName) device.name else "")
        customNameTextTextInputLayout.requestFocus()
        hideDeviceCheckBox.isChecked = device.isHidden

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            submitClickListener()
        }
    }

    private fun submitClickListener() {
        val deviceName = customNameTextTextInputLayout.editText?.text.toString()
        val isHidden = hideDeviceCheckBox.isChecked

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