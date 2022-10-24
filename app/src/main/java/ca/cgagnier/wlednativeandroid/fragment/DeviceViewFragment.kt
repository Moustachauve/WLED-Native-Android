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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import ca.cgagnier.wlednativeandroid.FileUploadContract
import ca.cgagnier.wlednativeandroid.FileUploadContractResult
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentDeviceViewBinding
import ca.cgagnier.wlednativeandroid.repository.DeviceViewModel


class DeviceViewFragment : Fragment() {

    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var _binding: FragmentDeviceViewBinding? = null
    private val binding get() = _binding!!

    private var shouldResetHistory = false

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
    }

    @SuppressLint("SetJavaScriptEnabled")
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
        setMenu()

        binding.deviceWebView.setBackgroundColor(Color.TRANSPARENT)

        // Prevent urls from opening in external browser
        binding.deviceWebView.webViewClient = object : WebViewClient() {
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
                    deviceViewModel.currentDevice.value?.let { view?.loadUrl(it.address) }
                } else if (shouldResetHistory) {
                    shouldResetHistory = false
                    view?.clearHistory()
                }
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                updateNavigationState()
            }
        }

        // Allows file upload
        binding.deviceWebView.webChromeClient = object : WebChromeClient() {
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

        binding.deviceWebView.settings.javaScriptEnabled = true
        binding.deviceWebView.settings.domStorageEnabled = true


        deviceViewModel.currentDevice.observe(this.viewLifecycleOwner) {
            Log.i(TAG_NAME, "observed device")
            // Let the "page finished" event load the new url
            binding.deviceWebView.loadUrl("about:blank")
            binding.deviceWebView.clearHistory()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                menu.findItem(R.id.action_browse_back).isEnabled = binding.deviceWebView.canGoBack()
                menu.findItem(R.id.action_browse_forward).isEnabled = binding.deviceWebView.canGoForward()
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
        if (binding.deviceWebView.canGoBack()) {
            binding.deviceWebView.goBack()
            return true
        }
        return false
    }

    fun navigateForward(): Boolean {
        if (binding.deviceWebView.canGoForward()) {
            binding.deviceWebView.goForward()
            return true
        }
        return false
    }

    fun updateNavigationState() {
        // TODO investigate crash when debugging caused by binding
        onBackPressedCallback.isEnabled = binding.deviceWebView.canGoBack()
        requireActivity().invalidateOptionsMenu()
    }

    companion object {
        const val TAG_NAME = "deviceWebview"
    }
}