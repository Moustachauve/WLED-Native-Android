package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Wifi (

	@Json(name = "bssid") val bssid : String,
	@Json(name = "rssi") val rssi : Int,
	@Json(name = "signal") val signal : Int,
	@Json(name = "channel") val channel : Int
)