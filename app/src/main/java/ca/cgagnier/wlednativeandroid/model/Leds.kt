package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Leds (

	@Json(name = "count") val count : Int,
	@Json(name = "rgbw") val isRgbw : Boolean,
	@Json(name = "wv") val isWhiteChannelEnabled : Boolean,
	@Json(name = "pwr") val estimatedPowerUsed : Int,
	@Json(name = "fps") val fps : Int,
	@Json(name = "maxpwr") val maxPower : Int,
	@Json(name = "maxseg") val maxSegment : Int
)