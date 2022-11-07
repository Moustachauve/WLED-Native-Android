package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Segment (

	@Json(name = "id") val id : Int? = null,
	@Json(name = "start") val start : Int? = null,
	@Json(name = "stop") val stop : Int? = null,
	@Json(name = "len") val length : Int? = null,
	@Json(name = "grp") val grouping : Int? = null,
	@Json(name = "spc") val spacing : Int? = null,
	@Json(name = "on") val isOn : Boolean? = null,
	@Json(name = "bri") val brightness : Int? = null,
	@Json(name = "col") val colors : List<List<Int>>? = null,
	@Json(name = "fx") val effect : Int? = null,
	@Json(name = "sx") val effectSpeed : Int? = null,
	@Json(name = "ix") val effectIntensity : Int? = null,
	@Json(name = "pal") val palette : Int? = null,
	@Json(name = "sel") val isSelected : Boolean? = null,
	@Json(name = "rev") val isReversed : Boolean? = null,
	@Json(name = "mi") val isMirrored : Boolean? = null
)