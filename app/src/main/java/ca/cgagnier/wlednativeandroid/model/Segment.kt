package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Segment (

	@Json(name = "id") val id : Int,
	@Json(name = "start") val start : Int,
	@Json(name = "stop") val stop : Int,
	@Json(name = "len") val length : Int,
	@Json(name = "grp") val grouping : Int,
	@Json(name = "spc") val spacing : Int,
	@Json(name = "on") val isOn : Boolean,
	@Json(name = "bri") val brightness : Int,
	@Json(name = "col") val colors : List<List<Int>>,
	@Json(name = "fx") val effect : Int,
	@Json(name = "sx") val effectSpeed : Int,
	@Json(name = "ix") val effectIntensity : Int,
	@Json(name = "pal") val palette : Int,
	@Json(name = "sel") val isSelected : Boolean,
	@Json(name = "rev") val isReversed : Boolean,
	@Json(name = "mi") val isMirrored : Boolean
)