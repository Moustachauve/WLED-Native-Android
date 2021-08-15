package ca.cgagnier.wlednativeandroid.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import ca.cgagnier.wlednativeandroid.R


class DeviceViewFragment : Fragment(R.layout.fragment_device_view) {

    lateinit var deviceWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val address = "http://192.168.10.194/"
        deviceWebView = view?.findViewById<WebView>(R.id.device_web_view)!!

        deviceWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
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
    }
}