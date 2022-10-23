package ca.cgagnier.wlednativeandroid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ca.cgagnier.wlednativeandroid.databinding.DeviceListMainEmptyBinding


class EmptyDataObserver(rv: RecyclerView?, ev: DeviceListMainEmptyBinding): RecyclerView.AdapterDataObserver() {

    private var emptyView: DeviceListMainEmptyBinding? = null
    private var recyclerView: RecyclerView? = null

    init {
        recyclerView = rv
        emptyView = ev
        checkIfEmpty()
    }


    private fun checkIfEmpty() {
        if (emptyView != null && recyclerView!!.adapter != null) {
            val emptyViewVisible = recyclerView!!.adapter!!.itemCount == 0
            emptyView!!.layout.visibility = if (emptyViewVisible) View.VISIBLE else View.GONE
            recyclerView!!.visibility = if (emptyViewVisible) View.GONE else View.VISIBLE
        }
    }

    override fun onChanged() {
        super.onChanged()
        checkIfEmpty()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onItemRangeChanged(positionStart, itemCount)
    }

}