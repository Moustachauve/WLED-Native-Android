package ca.cgagnier.wlednativeandroid.ui.components

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.cgagnier.wlednativeandroid.WebViewLiveData
import ca.cgagnier.wlednativeandroid.model.Device

class WebViewViewModel(appContext: Context) : ViewModel() {
    private val webView = WebViewLiveData(appContext)
    fun webView(): LiveData<WebView> = webView

    val backQueue = ArrayDeque<String>(5)
    var displayedDevice: Device? = null

    var firstLoad: Boolean
        get() {
            return webView.firstLoad
        }
        set(isFirstLoad) {
            webView.firstLoad = isFirstLoad
        }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WebViewViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WebViewViewModel(appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}