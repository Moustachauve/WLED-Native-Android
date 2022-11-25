package ca.cgagnier.wlednativeandroid.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.FileUploadContract
import ca.cgagnier.wlednativeandroid.FileUploadContractResult
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceViewBinding
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import ca.cgagnier.wlednativeandroid.viewmodel.WebViewViewModel
import com.google.android.material.appbar.MaterialToolbar


class DeviceViewFragment : Fragment() {

    private val deviceListViewModel: DeviceListViewModel by activityViewModels {
        DeviceListViewModelFactory(
            (requireActivity().application as DevicesApplication).repository,
            (requireActivity().application as DevicesApplication).userPreferencesRepository
        )
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var webViewViewModel: WebViewViewModel
    private lateinit var _webview: WebView

    private var _binding: FragmentDeviceViewBinding? = null
    private val binding get() = _binding!!

    private var shouldResetHistory = false
    private var shouldShowErrorPage = false

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (_webview.canGoBack()) {
                    navigateBack()
                } else {
                    isEnabled = false
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
        Log.i(TAG_NAME, "Device view creating")
        _binding = FragmentDeviceViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG_NAME, "Device view created")
        setMenu(binding.deviceToolbar)
        updateTitle()

        ViewCompat.setOnApplyWindowInsetsListener(binding.deviceToolbarContainer) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        var fromRestore = false
        val webViewFactory = WebViewViewModel.Factory(requireActivity())
        webViewViewModel = ViewModelProvider(this, webViewFactory)[WebViewViewModel::class.java]
        webViewViewModel.webView().observe(viewLifecycleOwner) { webView: WebView ->
            _webview = webView

            if (!webViewViewModel.firstLoad) {
                Log.i(TAG_NAME, "Webview restored")
                fromRestore = true
            } else {
                Log.i(TAG_NAME, "Webview first load")
                webViewViewModel.firstLoad = false
                webView.setBackgroundColor(Color.TRANSPARENT)

                // Prevent urls from opening in external browser
                webView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        if (view?.canGoBack() == true) {
                            val webBackForwardList = view.copyBackForwardList()
                            val currentIndex = webBackForwardList.currentIndex
                            if (webBackForwardList.getItemAtIndex(currentIndex - 1).url == request?.url.toString()) {
                                view.goBack()
                                return true
                            } else if (request?.url?.path == "/") {
                                view.goBackOrForward(-currentIndex)
                            }
                        }
                        return false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        updateNavigationState()
                        Log.i(TAG_NAME, "page started $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        updateNavigationState()
                        Log.i(TAG_NAME, "page finished $url")
                        if (url == "about:blank") {
                            Log.i(TAG_NAME, "page finished - cleared history")
                            shouldResetHistory = true
                            if (shouldShowErrorPage) {
                                shouldShowErrorPage = false
                                view?.loadUrl("file:///android_asset/device_error.html")
                            } else {
                                deviceListViewModel.activeDevice.value?.let { view?.loadUrl(it.address) }
                            }
                        } else if (shouldResetHistory) {
                            shouldResetHistory = false
                            view?.clearHistory()
                            updateNavigationState()
                        }
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean
                    ) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        updateNavigationState()
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            Log.i(TAG_NAME,
                                "Error received ${request.url} - ${error?.description}")


                            shouldShowErrorPage = true
                            view?.loadUrl("about:blank")
                            view?.clearHistory()
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

                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true

                if (savedInstanceState != null && !fromRestore) {
                    savedInstanceState.getBundle(BUNDLE_WEBVIEW_STATE)
                        ?.let {
                            Log.i(TAG_NAME, "Restoring webview from bundle")
                            _webview.restoreState(it)
                            fromRestore = true
                        }
                }
            }

            binding.deviceWebViewContainer.addView(webView)


        }

        deviceListViewModel.activeDevice.observe(viewLifecycleOwner) {
            if (fromRestore) {
                fromRestore = false
                return@observe
            }
            if (!deviceListViewModel.expectDeviceChange) {
                Log.i(TAG_NAME, "observed device, but did not expect changes")
                return@observe
            }
            Log.i(TAG_NAME, "observed device")
            deviceListViewModel.expectDeviceChange = false
            // Let the "page finished" event load the new url
            _webview.loadUrl("about:blank")
            _webview.clearHistory()

            updateTitle()
            updateNavigationState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(BUNDLE_WEBVIEW_STATE, webViewViewModel.bundle)
    }

    private fun setMenu(toolbar: MaterialToolbar) {
        toolbar.setupWithNavController(
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            onBackPressedCallback.isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
            onBackPressedCallback.isEnabled = true
        }

        deviceListViewModel.isTwoPane.observe(viewLifecycleOwner) {
            updateNavigationState()
        }

        toolbar.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                menu.findItem(R.id.action_browse_back).isEnabled = _webview.canGoBack()
                menu.findItem(R.id.action_browse_forward).isEnabled = _webview.canGoForward()

                if (deviceListViewModel.isTwoPane.value == true) {
                    toolbar.navigationIcon = null
                }
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.navigate, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_browse_back -> {
                        navigateBack()
                        true
                    }
                    R.id.action_browse_forward -> {
                        navigateForward()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun updateTitle() {
        binding.deviceToolbar.title =
            deviceListViewModel.activeDevice.value?.name ?: getString(R.string.select_a_device)
        binding.deviceToolbar.subtitle = deviceListViewModel.activeDevice.value?.address
    }

    fun navigateBack(): Boolean {
        if (_webview.canGoBack()) {
            _webview.goBack()
            return true
        }
        return false
    }

    fun navigateForward(): Boolean {
        if (_webview.canGoForward()) {
            _webview.goForward()
            return true
        }
        return false
    }

    fun updateNavigationState() {
        _binding?.deviceToolbar?.invalidateMenu()
    }

    companion object {
        const val TAG_NAME = "deviceWebview"
        const val BUNDLE_WEBVIEW_STATE = "bundleWebviewStateKey"
    }
}