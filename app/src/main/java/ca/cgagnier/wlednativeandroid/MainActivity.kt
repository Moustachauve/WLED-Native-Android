package ca.cgagnier.wlednativeandroid

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import ca.cgagnier.wlednativeandroid.databinding.ActivityMainBinding
import ca.cgagnier.wlednativeandroid.repository_old.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceApi.setApplication(application as DevicesApplication)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainerView) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            windowInsets
        }

        initDevices()

        var isConnectedToWledAP: Boolean
        try {
            isConnectedToWledAP = DeviceDiscovery.isConnectedToWledAP(applicationContext)
        } catch (e: Exception) {
            isConnectedToWledAP = false
            Log.e(TAG, "Error when checking isConnectedToWledAP: " + e.message, e)
        }

        if (isConnectedToWledAP) {
            val connectionManager =
                applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?

            val request = NetworkRequest.Builder()
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

            connectionManager!!.requestNetwork(
                request.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        try {
                            connectionManager.bindProcessToNetwork(network)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }

        setContentView(binding.root)
    }

    private fun initDevices() {
        DeviceRepository.init(applicationContext)
    }

    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }
}