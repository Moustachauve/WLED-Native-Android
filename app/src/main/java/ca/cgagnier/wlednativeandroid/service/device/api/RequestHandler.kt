package ca.cgagnier.wlednativeandroid.service.device.api

import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.Request
import ca.cgagnier.wlednativeandroid.service.device.api.request.SoftwareUpdateRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.StateChangeRequest

abstract class RequestHandler {

    suspend fun processRequest(request: Request) {
        when(request) {
            is RefreshRequest -> handleRefreshRequest(request)
            is StateChangeRequest -> handleChangeStateRequest(request)
            is SoftwareUpdateRequest -> handleSoftwareUpdateRequest(request)
            else -> throw Exception("Unknown request type: ${request.javaClass}")
        }
    }

    abstract suspend fun handleRefreshRequest(request: RefreshRequest)
    abstract suspend fun handleChangeStateRequest(request: StateChangeRequest)
    abstract suspend fun handleSoftwareUpdateRequest(request: SoftwareUpdateRequest)
}