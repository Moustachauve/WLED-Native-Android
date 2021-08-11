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

        val wledList = loadWledList()
        val wledListRecyclerView = findViewById<RecyclerView>(R.id.wled_list_recycler_view)
        val layoutManager = LinearLayoutManager(this)

        wledListRecyclerView.adapter = WledListAdapter(wledList)
        wledListRecyclerView.layoutManager = layoutManager
        wledListRecyclerView.setHasFixedSize(true)

        val dividerItemDecoration = DividerItemDecoration(
            wledListRecyclerView.context,
            layoutManager.orientation
        )
        wledListRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    private fun loadWledList(): List<WledListItem> {
        val list = ArrayList<WledListItem>()

        for (i in 0 until 66) {
            val fakeIp = i + 104
            val item = WledListItem("Wled $i", "192.168.1.$fakeIp", 127, i % 3 <= 1)
            list += item
        }

        return list
    }
}