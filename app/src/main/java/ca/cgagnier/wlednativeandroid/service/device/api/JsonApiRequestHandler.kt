package ca.cgagnier.wlednativeandroid.service.device.api

import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.SoftwareUpdateRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.StateChangeRequest

class JsonApiRequestHandler(application: DevicesApplication) : RequestHandler(application) {
    override suspend fun handleRefreshRequest(request: RefreshRequest) {
        val refreshedDevice = DeviceApiService.fromApplication(application)
            .refresh(request.device, request.silentRefresh, request.saveChanges)
        request.callback?.invoke(refreshedDevice)
    }

    override suspend fun handleChangeStateRequest(request: StateChangeRequest) {
        DeviceApiService.fromApplication(application)
            .postJson(request.device, request.state, request.saveChanges)
    }

    override suspend fun handleSoftwareUpdateRequest(request: SoftwareUpdateRequest) {
        val response = DeviceApiService.fromApplication(application)
            .installUpdate(request.device, request.binaryFile)
        request.callback?.invoke(response)
    }
}