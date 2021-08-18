package ca.cgagnier.wlednativeandroid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceItem(
    @Json(name = "address")
    val address: String
    ) {

    @Json(name = "name")
    var name: String = ""

    @Transient
    var brightness: Int = 0

    @Transient
    var isPoweredOn: Boolean = false
}
