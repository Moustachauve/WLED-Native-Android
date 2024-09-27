package ca.cgagnier.wlednativeandroid.service.device

import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.service.device.api.RequestsManager
import javax.inject.Inject

/**
 * Stores the transient information about the state of a device
 */
class State(val device: Device) {
    @Inject
    lateinit var requestsManager: RequestsManager
}