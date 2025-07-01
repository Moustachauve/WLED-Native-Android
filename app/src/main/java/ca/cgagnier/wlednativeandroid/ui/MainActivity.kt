package ca.cgagnier.wlednativeandroid.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.FileUploadContract
import ca.cgagnier.wlednativeandroid.FileUploadContractResult
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.DeviceControlsProviderService
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import ca.cgagnier.wlednativeandroid.ui.theme.WLEDNativeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject
    lateinit var versionWithAssetsRepository: VersionWithAssetsRepository

    private val _deviceMacAddress = MutableStateFlow<String?>(null)
    val deviceMacAddress: StateFlow<String?> = _deviceMacAddress.asStateFlow()

    // For WebView file upload support
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
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Handle navigation from Device Controls on Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val deviceMacAddress = intent.getStringExtra(DeviceControlsProviderService.EXTRA_DEVICE_MAC)
            _deviceMacAddress.value = deviceMacAddress
        }

        setContent {
            WLEDNativeTheme {
                MainNavHost(deviceMacAddress = deviceMacAddress)
            }
        }
        updateDeviceVersionList()
    }

    /**
     * Checks for device updates once in a while
     */
    private fun updateDeviceVersionList() {
        lifecycleScope.launch(Dispatchers.IO) {
            userPreferencesRepository.lastUpdateCheckDate.collect {
                val now = System.currentTimeMillis()
                if (now < it) {
                    Log.i(TAG, "Not updating version list since it was done recently.")
                    return@collect
                }
                val releaseService = ReleaseService(versionWithAssetsRepository)
                releaseService.refreshVersions(applicationContext.cacheDir)
                // Set the next date to check in minimum 24 hours from now.
                userPreferencesRepository.updateLastUpdateCheckDate(now + (24 * 60 * 60 * 1000))
            }
        }
    }
}