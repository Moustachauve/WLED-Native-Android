package ca.cgagnier.wlednativeandroid.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository


class DeviceViewFragment : Fragment(R.layout.fragment_device_view) {

    lateinit var deviceWebView: WebView
    lateinit var attachedDevice: DeviceItem

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(BUNDLE_ADDRESS_KEY)?.let {
            attachedDevice = DeviceRepository.get(it)!!
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

        deviceWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            var settingToSet = WebSettingsCompat.FORCE_DARK_AUTO
            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> settingToSet = WebSettingsCompat.FORCE_DARK_ON
                Configuration.UI_MODE_NIGHT_NO -> settingToSet = WebSettingsCompat.FORCE_DARK_OFF
            }
            WebSettingsCompat.setForceDark(deviceWebView.settings, settingToSet)
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