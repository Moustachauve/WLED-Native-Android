package ca.cgagnier.wlednativeandroid.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.FileUploadContract
import ca.cgagnier.wlednativeandroid.FileUploadContractResult
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository


class DeviceViewFragment : Fragment(R.layout.fragment_device_view) {

    private lateinit var deviceWebView: WebView
    private lateinit var attachedDevice: DeviceItem

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    var uploadMessage: ValueCallback<Array<Uri>>? = null

    val fileUpload = registerForActivityResult(FileUploadContract()) { result: FileUploadContractResult ->
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
                navigateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        arguments?.getString(BUNDLE_ADDRESS_KEY)?.let {
            val fromSavedDevices = DeviceRepository.get(it)
            if (fromSavedDevices != null) {
                attachedDevice = fromSavedDevices
                return
            }

            // It should always be an address. If we don't have it saved, try to open it anyway
            attachedDevice = DeviceItem(it)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val address = attachedDevice.getDeviceUrl()
        deviceWebView = view?.findViewById<WebView>(R.id.device_web_view)!!

        deviceWebView.setBackgroundColor(Color.TRANSPARENT)

        // Prevent urls from opening in external browser
        deviceWebView.webViewClient = object : WebViewClient() {
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
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                updateNavigationState()
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                updateNavigationState()
            }
        }

        // Allows file upload
        deviceWebView.webChromeClient = object : WebChromeClient() {
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

        deviceWebView.settings.javaScriptEnabled = true
        deviceWebView.settings.domStorageEnabled = true
        deviceWebView.loadUrl(address)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                menu.findItem(R.id.action_browse_back).isEnabled = deviceWebView.canGoBack()
                menu.findItem(R.id.action_browse_forward).isEnabled = deviceWebView.canGoForward()
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
                        onBackPressedCallback.isEnabled = false
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun navigateBack(): Boolean {
        if (deviceWebView.canGoBack()) {
            deviceWebView.goBack()
            return true
        }
        return false
    }

    fun navigateForward(): Boolean {
        if (deviceWebView.canGoForward()) {
            deviceWebView.goForward()
            return true
        }
        return false
    }

    fun updateNavigationState() {
        onBackPressedCallback.isEnabled = deviceWebView.canGoBack()
        requireActivity().invalidateOptionsMenu()
    }

    companion object {
        const val TAG_NAME = "deviceWebview"
        const val BUNDLE_ADDRESS_KEY = "bundleDeviceAddressKey"

        @JvmStatic
        fun newInstance(device: DeviceItem) = DeviceViewFragment().apply {
            arguments = Bundle().apply {
                putString(BUNDLE_ADDRESS_KEY, device.address)
            }
        }
    }
}