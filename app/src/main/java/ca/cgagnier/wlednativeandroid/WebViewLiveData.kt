package ca.cgagnier.wlednativeandroid

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.LiveData

// Solution from https://medium.com/@nicholas.rose/keeping-webview-state-across-configuration-changes-8e071ee9de86
// Allows to keep the webview alive through device rotation and activity recreation
class WebViewLiveData(appContext: Context) : LiveData<WebView>() {
    var firstLoad = true

    private val webView: WebView = WebView(appContext)

    override fun onActive() {
        value = webView
    }

    override fun onInactive() {
        webView.detachFromParent()
    }

    private fun WebView.detachFromParent() {
        (parent as? ViewGroup)?.removeView(this)
    }
}