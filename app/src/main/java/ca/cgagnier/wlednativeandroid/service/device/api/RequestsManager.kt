package ca.cgagnier.wlednativeandroid.service.device.api

import android.util.Log
import ca.cgagnier.wlednativeandroid.service.device.api.request.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

private const val TAG = "RequestsManager"

class RequestsManager(
    private val id: String,
    private val requestHandler: RequestHandler
) {
    // TODO: Add websocket support
    private val requestQueue = ArrayDeque<Request>()

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext(TAG))
    private var locked = false

    fun addRequest(request: Request) {
        requestQueue.add(request)
        Log.d(TAG, "[$id] Added new request: ${request.javaClass} (#${requestQueue.size})")
        processAllRequests()
    }

    private fun processAllRequests() {
        scope.launch(Dispatchers.IO) {
            var canProcessMore = true
            while (requestQueue.isNotEmpty() && canProcessMore) {
                canProcessMore = processRequests()
                Log.d(TAG, "[$id] Done request, can continue: $canProcessMore (${requestQueue.size} left)")
            }
        }
    }

    private suspend fun processRequests(): Boolean {
        if (locked) {
            return false
        }
        locked = true
        try {
            if (requestQueue.isEmpty()) {
                return false
            }
            Log.d(TAG, "[$id] Processing a request")
            requestHandler.processRequest(requestQueue.removeFirst())
            return true
        } finally {
            locked = false
        }
    }
}