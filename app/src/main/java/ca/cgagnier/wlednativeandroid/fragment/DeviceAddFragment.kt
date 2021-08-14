package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import ca.cgagnier.wlednativeandroid.R


class DeviceAddFragment : Fragment(R.layout.fragment_device_add) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addManuallyButton = view.findViewById<Button>(R.id.add_manually_button)

        addManuallyButton.setOnClickListener {
            val dialog = DeviceAddManuallyFragment()
            dialog.showsDialog = true
            dialog.show(childFragmentManager, "device_add_manually")
        }
    }
}