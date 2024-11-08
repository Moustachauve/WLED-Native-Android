package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Wifi (

	@Json(name = "bssid") val bssid : String? = null,
	@Json(name = "rssi") val rssi : Int? = null,
	@Json(name = "signal") val signal : Int? = null,
	@Json(name = "channel") val channel : Int? = null,
	@Json(name = "ap") val isApMode : Boolean? = null,
)