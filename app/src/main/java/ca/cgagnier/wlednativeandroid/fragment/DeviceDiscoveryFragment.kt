package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery


class DeviceDiscoveryFragment : Fragment(R.layout.fragment_device_discovery),
    DeviceAddManuallyFragment.NoticeDialogListener {

    private lateinit var listener: DeviceAddManuallyFragment.NoticeDialogListener
    lateinit var deviceDiscovery: DeviceDiscovery

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DeviceAddManuallyFragment.NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }

        deviceDiscovery = DeviceDiscovery(context)
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

    override fun onPause() {
        deviceDiscovery.stop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        deviceDiscovery.start()
    }

    override fun onDestroy() {
        deviceDiscovery.stop()
        super.onDestroy()
    }

    override fun onDeviceManuallyAdded(dialog: DialogFragment) = listener.onDeviceManuallyAdded(dialog)
}