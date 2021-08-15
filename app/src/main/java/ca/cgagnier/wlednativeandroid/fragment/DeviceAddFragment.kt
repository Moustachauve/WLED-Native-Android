package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import ca.cgagnier.wlednativeandroid.R


class DeviceAddFragment : Fragment(R.layout.fragment_device_add),
    DeviceAddManuallyFragment.NoticeDialogListener {

    internal lateinit var listener: DeviceAddManuallyFragment.NoticeDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DeviceAddManuallyFragment.NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addManuallyButton = view.findViewById<Button>(R.id.add_manually_button)

        addManuallyButton.setOnClickListener {
            val dialog = DeviceAddManuallyFragment()
            dialog.showsDialog = true
            dialog.show(childFragmentManager, "device_add_manually")
        }
    }

    override fun onDeviceManuallyAdded(dialog: DialogFragment) = listener.onDeviceManuallyAdded(dialog)
}