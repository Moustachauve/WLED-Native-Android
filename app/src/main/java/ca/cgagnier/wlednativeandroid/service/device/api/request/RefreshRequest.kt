package ca.cgagnier.wlednativeandroid.service.device.api.request

import ca.cgagnier.wlednativeandroid.model.Device

class RefreshRequest(
    device: Device,
    val silentRefresh: Boolean = false,
    val saveChanges: Boolean = true,
    val callback: (suspend (Device) -> Unit)? = null
) : Request(device)