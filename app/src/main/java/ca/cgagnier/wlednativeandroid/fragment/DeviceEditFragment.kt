package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class DeviceEditFragment : DialogFragment() {

    lateinit var deviceAddressTextInputLayout: TextInputLayout
    lateinit var customNameTextTextInputLayout: TextInputLayout
    lateinit var device: DeviceItem

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it, R.style.Theme_WLEDNativeAndroid_Dialog_Alert)
            builder.setMessage(R.string.add_a_device_manually)
                .setPositiveButton(getString(R.string.save_device), null)
                .setNegativeButton(R.string.cancel, null)
                .setView(R.layout.fragment_device_add_edit)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(DeviceViewFragment.BUNDLE_ADDRESS_KEY)?.let {
            device = DeviceRepository.get(it)!!
        }
    }

    override fun onResume() {
        super.onResume()

        val alertDialog = dialog as AlertDialog

        deviceAddressTextInputLayout = alertDialog.findViewById<TextInputLayout>(R.id.device_address_text_input_layout)!!
        customNameTextTextInputLayout = alertDialog.findViewById<TextInputLayout>(R.id.custom_name_text_input_layout)!!

        deviceAddressTextInputLayout.isEnabled = false
        deviceAddressTextInputLayout.editText?.setText(device.address)
        customNameTextTextInputLayout.editText?.setText(if (device.isCustomName) device.name else "")

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            submitClickListener()
        }
    }

    private fun submitClickListener() {
        val deviceName = customNameTextTextInputLayout.editText?.text.toString()

        val device = device.copy(
            name = deviceName,
            isCustomName = deviceName != ""
        )

        DeviceRepository.put(device)
        DeviceApi.update(device)

        dismiss()
    }

    companion object {
        private const val BUNDLE_ADDRESS_KEY = "bundleDeviceAddressKey"

        @JvmStatic
        fun newInstance(device: DeviceItem) = DeviceEditFragment().apply {
            arguments = Bundle().apply {
                putString(BUNDLE_ADDRESS_KEY, device.address)
            }
        }
    }
}