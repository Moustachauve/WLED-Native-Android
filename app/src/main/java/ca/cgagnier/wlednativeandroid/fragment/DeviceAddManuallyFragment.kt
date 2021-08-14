package ca.cgagnier.wlednativeandroid.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ca.cgagnier.wlednativeandroid.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeviceAddManuallyFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it, R.style.Theme_WLEDNativeAndroid_Dialog_Alert)
            builder.setMessage(R.string.add_a_device_manually)
                .setPositiveButton("Add Device",
                    DialogInterface.OnClickListener { dialog, id ->
                        // TODO: add the device!
                    })
                .setNegativeButton(R.string.cancel, null)
                .setView(R.layout.fragment_device_add_manually)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
}