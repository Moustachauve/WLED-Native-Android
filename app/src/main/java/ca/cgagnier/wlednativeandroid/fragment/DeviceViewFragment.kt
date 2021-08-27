package ca.cgagnier.wlednativeandroid.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import android.webkit.ValueCallback
import ca.cgagnier.wlednativeandroid.FileUploadContract
import ca.cgagnier.wlednativeandroid.FileUploadContractResult


class DeviceViewFragment : Fragment(R.layout.fragment_device_view) {

    private lateinit var deviceWebView: WebView
    private lateinit var attachedDevice: DeviceItem

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
                return false
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

    fun onBackPressed(): Boolean {
        if (deviceWebView.canGoBack()) {
            deviceWebView.goBack()
            return true
        }
        return false
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