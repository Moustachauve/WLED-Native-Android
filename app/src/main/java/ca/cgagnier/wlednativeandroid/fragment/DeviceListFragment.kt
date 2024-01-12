package ca.cgagnier.wlednativeandroid.fragment

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.cgagnier.wlednativeandroid.AutoDiscoveryActivity
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.adapter.DeviceListAdapter
import ca.cgagnier.wlednativeandroid.adapter.RecyclerViewAnimator
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceListBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class DeviceListFragment : Fragment(),
    SwipeRefreshLayout.OnRefreshListener {

    private val deviceRepository: DeviceRepository by lazy {
        (requireActivity().application as DevicesApplication).deviceRepository
    }
    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).deviceRepository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository
        )
    }

    private var _binding: FragmentDeviceListBinding? = null
    private val binding get() = _binding!!
    private var layoutChangedListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private val loopHandler = Handler(Looper.getMainLooper())

    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var hasDoneFirstRefresh = false

    private var pastDeviceListWidth: Int? = null

    override fun onResume() {
        super.onResume()
        refreshListFromApi(false)
        checkIfConnectedInAPMode(true)

        Log.i(TAG, "Starting Refresh timer")
        refreshTimer(loopHandler, 10000)
    }

    override fun onPause() {
        Log.i(TAG, "Stopping Refresh timer")
        loopHandler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceListBinding.inflate(inflater, container, false)

        setFragmentResultListener(REQUEST_OPEN_DEVICE_KEY) { _, bundle ->
            onFragmentRequestOpenDevice(bundle)
        }
        setFragmentResultListener(REQUEST_LIST_VISIBLITY_TOGGLE) { _, _ ->
            Log.i(TAG, "Toggle list visibility request received")
            if (deviceListViewModel.isListHidden.value == true) {
                openDeviceList()
            } else {
                closeDeviceList()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(binding.root.context)

        binding.mainToolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }
        setMenu(binding.mainToolbar)
        setNavigationMenu(binding.navigationView, binding.drawerLayout)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainToolbar) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            windowInsets
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            // This weird workaround of calling setOnApplyWindowInsetsListener on the navigationView
            // is needed to make sure the DeviceViewFragment's insets are set properly on devices
            // with API < 30
            ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView) { _, windowInsets ->
                windowInsets
            }
        }

        swipeRefreshLayout = binding.swipeRefresh
        swipeRefreshLayout.setOnRefreshListener(this)

        val slidingPaneLayout = binding.slidingPaneLayout
        slidingPaneLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED



        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            DeviceListOnBackPressedCallback(slidingPaneLayout)
        )

        val deviceApi =
            DeviceApiService.fromApplication(requireActivity().application as DevicesApplication)
        deviceListAdapter = DeviceListAdapter(deviceApi) { device: Device ->
            openDevice(device)
        }


        binding.deviceListRecyclerView.adapter = deviceListAdapter
        binding.deviceListRecyclerView.layoutManager = layoutManager
        binding.deviceListRecyclerView.setHasFixedSize(true)
        binding.deviceListRecyclerView.itemAnimator = RecyclerViewAnimator()
        deviceListViewModel.selectedDevice?.let { selectedDevice ->
            deviceListAdapter.setSelectedDevice(selectedDevice)
        } ?: run {
            showSelectDeviceFragment()
        }

        deviceListViewModel.allDevices.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                deviceListAdapter.submitList(it)
            }
            swipeRefreshLayout.isRefreshing = false
            val isEmpty = devices?.isEmpty() == true
            binding.emptyDataParent.layout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.deviceListRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.swipeRefresh.isEnabled = !isEmpty

            if (!hasDoneFirstRefresh) {
                hasDoneFirstRefresh = true
                refreshListFromApi(true)
            }
        }

        deviceListAdapter.isSelectable = false

        binding.emptyDataParent.addDeviceButton.setOnClickListener {
            openAddDeviceFragment()
        }

        binding.apModeContainer.setOnClickListener {
            openDevice(DeviceDiscovery.getDefaultAPDevice())
        }

        layoutChangedListener = ViewTreeObserver.OnGlobalLayoutListener {
            deviceListAdapter.isSelectable = !slidingPaneLayout.isSlideable
            deviceListViewModel.isTwoPane.value = deviceListAdapter.isSelectable
            view.viewTreeObserver.removeOnGlobalLayoutListener(layoutChangedListener)
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(layoutChangedListener)
    }

    private fun refreshTimer(handler: Handler, delay: Long) {
        Log.i(TAG, "Refreshing devices from timer")
        refreshListFromApi(true)
        handler.postDelayed({ refreshTimer(handler, delay) }, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setMenu(toolbar: MaterialToolbar) {
        toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home, menu)
                val actionBar = requireActivity().actionBar

                actionBar?.setDisplayHomeAsUpEnabled(true)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_device_add -> {
                        openAddDeviceFragment()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setNavigationMenu(navigationView: NavigationView, drawerLayout: DrawerLayout) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_device_add -> {
                    openAddDeviceFragment()
                }

                R.id.action_refresh -> {
                    if (deviceListAdapter.itemCount > 0) {
                        swipeRefreshLayout.isRefreshing = true
                    }
                    onRefresh()
                }

                R.id.action_manage_device -> {
                    openManageDevicesFragment()
                }

                R.id.action_settings -> {
                    openSettings()
                }

                R.id.action_visit_help -> {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://kno.wled.ge/"))
                    startActivity(browserIntent)
                }

                R.id.action_visit_sponsor -> {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/sponsors/Moustachauve/")
                    )
                    startActivity(browserIntent)
                }
            }
            drawerLayout.close()
            true
        }
    }

    private fun openAddDeviceFragment() {
        val newDialog = DeviceAddManuallyFragment()
        newDialog.showsDialog = true
        newDialog.show(childFragmentManager, "device_add_manually")
    }

    private fun openDeviceList() {
        deviceListViewModel.isListHidden.value = false
        val anim = ResizeWidthAnimation(binding.drawerLayout, pastDeviceListWidth ?: 600)
        anim.duration = 150
        binding.drawerLayout.startAnimation(anim)
        //val params = binding.drawerLayout.layoutParams as SlidingPaneLayout.LayoutParams
        //params.width = 0
        //binding.drawerLayout.layoutParams = params
    }

    private fun closeDeviceList() {
        deviceListViewModel.isListHidden.value = true
        pastDeviceListWidth = binding.drawerLayout.width
        val anim = ResizeWidthAnimation(binding.drawerLayout, 0)
        anim.duration = 150
        binding.drawerLayout.startAnimation(anim)
        //val params = binding.drawerLayout.layoutParams as SlidingPaneLayout.LayoutParams
        //params.width = 0
        //binding.drawerLayout.layoutParams = params
    }

    private fun openManageDevicesFragment() {
        val dialog = ManageDeviceFragment()
        dialog.showsDialog = true
        dialog.show(parentFragmentManager, "device_manage")
    }

    private fun openSettings() {
        val dialog = SettingsFragment()
        dialog.showsDialog = true
        dialog.show(childFragmentManager, "device_manage")
    }

    private fun openDevice(device: Device) {
        Log.i(TAG, "Opening device ${device.address}")

        deviceListAdapter.isSelectable = !binding.slidingPaneLayout.isSlideable
        deviceListViewModel.isTwoPane.value = deviceListAdapter.isSelectable
        deviceListViewModel.selectedDevice = device

        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.device_web_view_fragment,
                DeviceViewFragment.newInstance(device.address))
            // If it's already open and the detail pane is visible, crossfade
            // between the fragments.
            if (binding.slidingPaneLayout.isOpen) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
        }

        binding.deviceListRecyclerView.scrollToPosition(
            deviceListAdapter.setSelectedDevice(device)
        )
        binding.slidingPaneLayout.openPane()
    }

    private fun showSelectDeviceFragment() {
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(
                R.id.device_web_view_fragment,
                DeviceNoSelectionFragment()
            )
            // If it's already open and the detail pane is visible, crossfade
            // between the fragments.
            if (binding.slidingPaneLayout.isOpen) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
        }
    }

    private fun openDevice(deviceAddress: String) {
        lifecycleScope.launch {
            val device = deviceRepository.findDeviceByAddress(deviceAddress)
            device?.let { openDevice(it) }
        }
    }

    override fun onRefresh() {
        (requireActivity() as AutoDiscoveryActivity).startAutoDiscovery()
        refreshListFromApi(false)
        checkIfConnectedInAPMode()
    }

    private fun refreshListFromApi(silentUpdate: Boolean) {
        if (deviceListViewModel.allDevices.value != null) {
            for (device in deviceListViewModel.allDevices.value!!) {
                DeviceApiService.fromApplication(requireActivity().application as DevicesApplication)
                    .update(device, silentUpdate)
            }
            hasDoneFirstRefresh = true
        }
    }

    private fun checkIfConnectedInAPMode(openDevice: Boolean = false) {
        Log.i(TAG, "Checking if connected to AP mode")
        var isConnectedToWledAP: Boolean
        try {
            isConnectedToWledAP = DeviceDiscovery.isConnectedToWledAP(requireContext())
        } catch (e: Exception) {
            isConnectedToWledAP = false
            Log.e(TAG, "Error in checkIfConnectedInAPMode: " + e.message, e)
            Firebase.crashlytics.recordException(e)
        }

        binding.apModeContainer.visibility = if (isConnectedToWledAP) View.VISIBLE else View.GONE

        if (isConnectedToWledAP) {
            Log.i(TAG, "Device is in AP Mode!")

            val connectionManager =
                requireContext().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager?

            val request = NetworkRequest.Builder()
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

            connectionManager!!.requestNetwork(
                request.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        try {
                            connectionManager.bindProcessToNetwork(network)
                            if (openDevice) {
                                openDevice(DeviceDiscovery.getDefaultAPDevice())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Firebase.crashlytics.recordException(e)
                        }
                    }
                })
        }
    }

    private fun onFragmentRequestOpenDevice(bundle: Bundle) {
        Log.i(TAG, "Open device request received")
        val deviceAddress = bundle.getString(DEVICE_ADDRESS)
        deviceAddress?.let {
            openDevice(deviceAddress)
        }
    }

    inner class DeviceListOnBackPressedCallback(
        private val slidingPaneLayout: SlidingPaneLayout
    ) : OnBackPressedCallback(
        // Set the default 'enabled' state to true only if it is slidable (i.e., the panes
        // are overlapping) and open (i.e., the detail pane is visible).
        slidingPaneLayout.isOpen
    ), SlidingPaneLayout.PanelSlideListener {

        init {
            slidingPaneLayout.addPanelSlideListener(this)
        }

        override fun handleOnBackPressed() {
            // Return to the list pane when the system back button is pressed.
            if (slidingPaneLayout.isOpen && slidingPaneLayout.isSlideable) {
                slidingPaneLayout.closePane()
                refreshListFromApi(false)
                return
            }

            isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
            isEnabled = slidingPaneLayout.isOpen
        }

        override fun onPanelSlide(panel: View, slideOffset: Float) {}

        override fun onPanelOpened(panel: View) {
            // Intercept the system back button when the detail pane becomes visible.
            isEnabled = true
        }

        override fun onPanelClosed(panel: View) {
            // Disable intercepting the system back button when the user returns to the
            // list pane.
            isEnabled = false
        }
    }

    class ResizeWidthAnimation(private val mView: View, private val mWidth: Int) : Animation() {
        private val mStartWidth: Int = mView.width

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            mView.layoutParams.width = mStartWidth + ((mWidth - mStartWidth) * interpolatedTime).toInt()
            mView.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    companion object {
        const val TAG = "DeviceListFragment"

        const val REQUEST_LIST_VISIBLITY_TOGGLE = "toggleListVisibility"
        const val REQUEST_OPEN_DEVICE_KEY = "openDevice"
        const val DEVICE_ADDRESS = "device_address"

        @JvmStatic
        fun createOpenDeviceBundle(deviceAddress: String): Bundle {
            return Bundle().apply {
                putString(DEVICE_ADDRESS, deviceAddress)
            }
        }
    }
}