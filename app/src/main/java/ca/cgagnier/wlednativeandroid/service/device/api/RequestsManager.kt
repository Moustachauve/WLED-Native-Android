package ca.cgagnier.wlednativeandroid.service.device.api

import android.util.Log
import ca.cgagnier.wlednativeandroid.AppContainer
import ca.cgagnier.wlednativeandroid.service.device.api.request.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class RequestsManager(appContainer: AppContainer) {
    // TODO: Add websocket support
    private val requestHandler: RequestHandler = JsonApiRequestHandler(appContainer)
    private val requestQueue = ArrayDeque<Request>()

    @OptIn(DelicateCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext(TAG))
    private var locked = false

    fun addRequest(request: Request) {
        requestQueue.add(request)
        Log.d(TAG, "Added new request: ${request.javaClass} (#${requestQueue.size})")
        processAllRequests()
    }

    private fun processAllRequests() {
        scope.launch {
            var canProcessMore = true
            while (requestQueue.isNotEmpty() && canProcessMore) {
                canProcessMore = processRequests()
                Log.d(TAG, "Done request, can continue: $canProcessMore")
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
            Log.d(TAG, "Processing a request")
            requestHandler.processRequest(requestQueue.removeFirst())
            return true
        } finally {
            locked = false
        }
    }

    companion object {
        const val TAG = "RequestsManager"
    }
}