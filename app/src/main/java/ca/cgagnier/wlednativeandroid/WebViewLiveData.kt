package ca.cgagnier.wlednativeandroid

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.LiveData

private const val TAG = "WebViewLiveData"

// Solution from https://medium.com/@nicholas.rose/keeping-webview-state-across-configuration-changes-8e071ee9de86
// Allows to keep the webview alive through device rotation and activity recreation
class WebViewLiveData(context: Context) : LiveData<WebView>() {
    var firstLoad = true

    private val webView: WebView = WebView(context)

    override fun onActive() {
        Log.d(TAG, "onActive")
        value = webView
    }

    override fun onInactive() {
        Log.d(TAG, "onInactive")
        //webView.detachFromParent()
    }

    private fun WebView.detachFromParent() {
        Log.d(TAG, "detachFromParent")
        (parent as? ViewGroup)?.removeView(this)
    }
}