package ca.cgagnier.wlednativeandroid.service.device.api.request

import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost

class StateChangeRequest(
    device: Device, val state: JsonPost, val saveChanges: Boolean = true
) : Request(device)