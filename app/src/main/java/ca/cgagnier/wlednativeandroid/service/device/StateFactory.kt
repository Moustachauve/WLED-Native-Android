package ca.cgagnier.wlednativeandroid.service.device

import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import ca.cgagnier.wlednativeandroid.service.device.api.JsonApiRequestHandler
import ca.cgagnier.wlednativeandroid.service.device.api.RequestsManager

class StateFactory(private val requestHandler: JsonApiRequestHandler) {

    private var allStates = mutableMapOf<String, State>()

    fun getState(device: StatefulDevice): State {
        if (!allStates.contains(device.address)) {
            allStates[device.address] = State(
                device,
                RequestsManager("${device.name}-${device.address}", requestHandler)
            )
        }

        return allStates[device.address]!!
    }
}