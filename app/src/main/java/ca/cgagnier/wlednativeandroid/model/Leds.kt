package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Leds (

	@Json(name = "count") val count : Int? = null,
	@Json(name = "rgbw") val isRgbw : Boolean? = null,
	@Json(name = "wv") val isWhiteChannelEnabled : Boolean? = null,
	@Json(name = "pwr") val estimatedPowerUsed : Int? = null,
	@Json(name = "fps") val fps : Int? = null,
	@Json(name = "maxpwr") val maxPower : Int? = null,
	@Json(name = "maxseg") val maxSegment : Int? = null
)