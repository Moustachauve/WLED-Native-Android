package ca.cgagnier.wlednativeandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(R.drawable.wled_logo_akemi)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_delete_outline_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val deviceList = loadDeviceList()
        val deviceListRecyclerView = findViewById<RecyclerView>(R.id.device_list_recycler_view)
        val layoutManager = LinearLayoutManager(this)

        deviceListRecyclerView.adapter = DeviceListAdapter(deviceList)
        deviceListRecyclerView.layoutManager = layoutManager
        deviceListRecyclerView.setHasFixedSize(true)

        val dividerItemDecoration = DividerItemDecoration(
            deviceListRecyclerView.context,
            layoutManager.orientation
        )
        deviceListRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    private fun loadDeviceList(): List<DeviceListItem> {
        val list = ArrayList<DeviceListItem>()

        for (i in 0 until 66) {
            val fakeIp = i + 104
            val item = DeviceListItem("Wled $i", "192.168.1.$fakeIp", 127, i % 3 <= 1)
            list += item
        }

        return list
    }
}