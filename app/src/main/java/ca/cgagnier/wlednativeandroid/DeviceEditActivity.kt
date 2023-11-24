package ca.cgagnier.wlednativeandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.cgagnier.wlednativeandroid.fragment.DeviceEditFragment

class DeviceEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS) ?: return

        setContentView(R.layout.activity_edit_device)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DeviceEditFragment.newInstance(deviceAddress))
                .commitNow()
        }
    }

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
    }
}