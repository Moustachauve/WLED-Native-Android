package ca.cgagnier.wlednativeandroid.fragment

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import ca.cgagnier.wlednativeandroid.DeviceEditActivity
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.FileUploadContract
import ca.cgagnier.wlednativeandroid.FileUploadContractResult
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceViewBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceViewViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceViewViewModelFactory
import ca.cgagnier.wlednativeandroid.viewmodel.WebViewViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date


class DeviceViewFragment : Fragment() {

    private val deviceRepository: DeviceRepository by lazy {
        (requireActivity().application as DevicesApplication).deviceRepository
    }
    private val deviceViewViewModel: DeviceViewViewModel by viewModels {
        DeviceViewViewModelFactory()
    }
    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            deviceRepository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository
        )
    }

    private lateinit var webViewViewModel: WebViewViewModel
    private lateinit var _webview: WebView

    private var initialLoad = true
    private lateinit var deviceAddress: String
    private lateinit var device: Device

    private var isLargeLayout: Boolean = false
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var _binding: FragmentDeviceViewBinding? = null
    private val binding get() = _binding!!

    var uploadMessage: ValueCallback<Array<Uri>>? = null

    val fileUpload =
        registerForActivityResult(FileUploadContract()) { result: FileUploadContractResult ->
            uploadMessage?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    result.resultCode,
                    result.intent
                )
            )
            uploadMessage = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceAddress = it.getString(DEVICE_ADDRESS)!!
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webCanGoBack()) {
                    webGoBack()
                } else {
                    isEnabled = false
                    deviceViewViewModel.loadingCounter = 0
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val isDebuggable = 0 != requireActivity().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        WebView.setWebContentsDebuggingEnabled(isDebuggable)
        Log.i(TAG, "Device view creating")
        _binding = FragmentDeviceViewBinding.inflate(layoutInflater, container, false)
        isLargeLayout = resources.getBoolean(R.bool.large_layout)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "Device view created")

        ViewCompat.setOnApplyWindowInsetsListener(binding.deviceToolbarContainer) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        val webViewFactory = WebViewViewModel.Factory(requireActivity())
        webViewViewModel = ViewModelProvider(this, webViewFactory)[WebViewViewModel::class.java]
        webViewViewModel.webView().observe(viewLifecycleOwner) { webView: WebView ->
            _webview = webView
            loadDevice()

            if (webViewViewModel.firstLoad) {
                Log.i(TAG, "Webview first load")
                webViewViewModel.firstLoad = false
                webView.setBackgroundColor(Color.TRANSPARENT)

                // Prevent urls from opening in external browser
                webView.webViewClient = object : WebViewClient() {

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        updateNavigationState()
                        Log.i(
                            TAG,
                            "page started $url, counter: ${deviceViewViewModel.loadingCounter}"
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        updateNavigationState()
                        deviceViewViewModel.loadingCounter--
                        Log.i(
                            TAG,
                            "page finished $url, counter: ${deviceViewViewModel.loadingCounter}"
                        )
                        if (deviceViewViewModel.loadingCounter <= 0) {
                            _binding?.pageLoadingIndicator?.visibility = View.GONE
                            deviceViewViewModel.loadingCounter = 0
                        }
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean
                    ) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        Log.i(TAG, "doUpdateVisitedHistory $url, isReload: $isReload")

                        if (url != null && !isReload) {
                            if (deviceViewViewModel.isGoingBack) {
                                deviceViewViewModel.isGoingBack = false
                            } else if (deviceViewViewModel.currentUrl.isNotEmpty()) {
                                deviceViewViewModel.backQueue.addLast(deviceViewViewModel.currentUrl)
                            }
                            filterBackQueue(url)

                            deviceViewViewModel.currentUrl = url
                        }
                        updateNavigationState()
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            Log.i(
                                TAG,
                                "Error received ${request.url} - ${error?.description}"
                            )

                            showLoadingIndicator()
                            view?.loadUrl("file:///android_asset/device_error.html")
                        }
                        super.onReceivedError(view, request, error)
                    }
                }

                // Allows file upload
                webView.webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        uploadMessage = filePathCallback
                        fileUpload.launch(123)
                        return true
                    }
                }

                webView.setDownloadListener { url, _, contentDisposition, mimetype, _ ->
                    val request = DownloadManager.Request(
                        Uri.parse(url)
                    )
                    request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )

                    @SuppressLint("SimpleDateFormat")
                    val formatter = SimpleDateFormat("yyyyMMdd")
                    val currentDate = formatter.format(Date())
                    val deviceName = URLEncoder.encode(device.name, "UTF-8")
                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                    val fullFilename = "${deviceName}_${currentDate}_${fileName}"
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fullFilename
                    )
                    val dm = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                    dm?.enqueue(request)
                    context?.let {
                        Toast.makeText(
                            it,
                            getString(R.string.downloading_file),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
            } else {
                Log.i(TAG, "Webview restored")
            }

            binding.deviceWebViewContainer.addView(webView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun loadDevice() {
        deviceRepository.findLiveDeviceByAddress(deviceAddress).asLiveData()
            .observe(viewLifecycleOwner) {
                it?.let { newDevice ->
                    device = newDevice
                    if (initialLoad) {
                        initialLoad = false
                        initialLoad()
                    } else {
                        updateTitle()
                    }
                }
            }
    }

    private fun initialLoad() {
        Log.i(TAG, "initialLoad")
        setMenu(binding.deviceToolbar)
        updateTitle()

        if (!deviceViewViewModel.webAlreadyLoaded) {
            deviceViewViewModel.webAlreadyLoaded = true
            showLoadingIndicator()
            Log.i(TAG, "initialLoad Requesting '${device.address}'")
            _webview.loadUrl("http://${device.address}")
        }
    }

    private fun setMenu(toolbar: MaterialToolbar) {
        toolbar.setupWithNavController(
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            if (deviceListViewModel.isTwoPane.value == true) {
                Log.d(TAG, "Requesting list to be hidden")
                setFragmentResult(DeviceListFragment.REQUEST_LIST_VISIBLITY_TOGGLE, Bundle())
            } else {
                Log.d(DeviceListFragment.TAG, "closing slidingPaneLayout")
                onBackPressedCallback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                onBackPressedCallback.isEnabled = true
            }
        }

        deviceListViewModel.isTwoPane.observe(viewLifecycleOwner) {
            updateNavigationState()
        }
        deviceListViewModel.isListHidden.observe(viewLifecycleOwner) {
            updateNavigationState()
        }

        toolbar.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                updateMenuState(menu)
            }

            @SuppressLint("RestrictedApi")
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.navigate, menu)

                if (menu is MenuBuilder) {
                    menu.setOptionalIconsVisible(true)
                }
                updateMenuState(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_browse_refresh -> {
                        Log.i(TAG, "Manual refresh requested")
                        refresh()
                        true
                    }

                    R.id.action_browse_update -> {
                        showUpdateDialog()
                        true
                    }

                    R.id.action_manage_device -> {
                        showEditDevice()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }

            fun updateMenuState(menu: Menu) {
                // Handle for example visibility of menu items
                menu.findItem(R.id.action_browse_update).isVisible = device.hasUpdateAvailable()
                if (deviceListViewModel.isTwoPane.value == true) {
                    if (deviceListViewModel.isListHidden.value == true) {
                        toolbar.setNavigationIcon(R.drawable.dock_to_right_24)
                    } else {
                        toolbar.setNavigationIcon(R.drawable.baseline_menu_open_24)
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun refresh() {
        updateTitle()
        Log.i(TAG, "refresh Requesting '${device.address}'")
        showLoadingIndicator()
        _webview.loadUrl("http://${device.address}")
    }

    private fun updateTitle() {
        Log.d(TAG, "Updating title")
        binding.deviceToolbar.title = device.name
        binding.deviceToolbar.subtitle = device.address
        updateNavigationState()
    }

    fun updateNavigationState() {
        _binding?.deviceToolbar?.invalidateMenu()
    }

    fun showLoadingIndicator() {
        _binding?.pageLoadingIndicator?.visibility = View.VISIBLE
        deviceViewViewModel.loadingCounter++
    }

    fun showUpdateDialog() {
        val fragmentManager = requireActivity().supportFragmentManager
        val deviceAddress = device.address
        val newFragment =
            DeviceUpdateAvailableFragment.newInstance(deviceAddress, isLargeLayout)
        newFragment.show(fragmentManager, "dialog")
    }

    fun showEditDevice() {
        val intent = Intent(requireActivity(), DeviceEditActivity::class.java)
        intent.putExtra(DeviceEditActivity.EXTRA_DEVICE_ADDRESS, device.address)
        startActivity(intent)
    }

    fun webCanGoBack(): Boolean {
        return deviceViewViewModel.backQueue.isNotEmpty()
    }

    fun webGoBack(): Boolean {
        if (webCanGoBack()) {
            val backUrl = deviceViewViewModel.backQueue.removeLast()
            _webview.loadUrl(backUrl)
            deviceViewViewModel.isGoingBack = true
            return true
        }
        return false
    }

    private fun filterBackQueue(currentUrl: String) {
        Log.i(TAG, "== Starting filter ========")
        Log.i(TAG, "Current Url: $currentUrl")
        Log.i(TAG, deviceViewViewModel.backQueue.toString())
        var i = deviceViewViewModel.backQueue.size
        for (url in deviceViewViewModel.backQueue.asReversed()) {
            i--
            if (url == currentUrl) {
                deviceViewViewModel.backQueue.subList(i, deviceViewViewModel.backQueue.size).clear()
                Log.i(TAG, "Removing up to $i")
                Log.i(TAG, deviceViewViewModel.backQueue.toString())
                return
            }
        }
    }

    companion object {
        const val TAG = "deviceWebview"

        private const val DEVICE_ADDRESS = "device_address"

        @JvmStatic
        fun newInstance(deviceAddress: String) =
            DeviceViewFragment().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ADDRESS, deviceAddress)
                }
            }
    }
}