package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.DeviceListAdapter
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery


class DeviceDiscoveryFragment : Fragment(R.layout.fragment_device_discovery),
    DeviceAddManuallyFragment.NoticeDialogListener,
    DeviceDiscovery.DeviceDiscoveredListener {

    private lateinit var listener: DeviceAddManuallyFragment.NoticeDialogListener
    lateinit var deviceDiscovery: DeviceDiscovery

    private val deviceListAdapter = DeviceListAdapter(ArrayList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DeviceAddManuallyFragment.NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement NoticeDialogListener"))
        }

        deviceDiscovery = DeviceDiscovery(context)
        deviceDiscovery.registerDeviceDiscoveredListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        deviceDiscovery.unregisterDeviceDiscoveredListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addManuallyButton = view.findViewById<Button>(R.id.add_manually_button)

        addManuallyButton.setOnClickListener {
            val dialog = DeviceAddManuallyFragment()
            dialog.showsDialog = true
            dialog.show(childFragmentManager, "device_add_manually")
        }

        val deviceFoundListRecyclerView = view.findViewById<RecyclerView>(R.id.device_found_list_recycler_view)
        val layoutManager = LinearLayoutManager(view.context)

        deviceFoundListRecyclerView.adapter = deviceListAdapter
        deviceFoundListRecyclerView.layoutManager = layoutManager
        deviceFoundListRecyclerView.setHasFixedSize(false)

        val dividerItemDecoration = DividerItemDecoration(
            deviceFoundListRecyclerView.context,
            layoutManager.orientation
        )
        deviceFoundListRecyclerView.addItemDecoration(dividerItemDecoration)
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

    override fun onDeviceDiscovered(serviceInfo: NsdServiceInfo) {
        val device = DeviceItem(serviceInfo.host.toString())
        if (DeviceRepository.contains(device)) {
            return
        }

        if (serviceInfo.serviceName != null) {
            device.name = serviceInfo.serviceName
        }

        DeviceRepository.add(device)

        activity?.runOnUiThread {
            deviceListAdapter.addItem(device)
        }
    }
}