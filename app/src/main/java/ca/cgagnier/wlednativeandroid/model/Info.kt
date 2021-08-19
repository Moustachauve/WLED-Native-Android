package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Info (

	@Json(name = "ver") val version : String,
	@Json(name = "vid") val buildId : Int,
	@Json(name = "leds") val leds : Leds,
	@Json(name = "str") val str : Boolean,
	@Json(name = "name") val name : String,
	@Json(name = "udpport") val udpPort : Int,
	@Json(name = "live") val isUpdatedLive : Boolean,
	@Json(name = "lm") val lm : String,
	@Json(name = "lip") val lip : String,
	@Json(name = "ws") val websocketClientCount : Int,
	@Json(name = "fxcount") val effectCount : Int,
	@Json(name = "palcount") val paletteCount : Int,
	@Json(name = "wifi") val wifi : Wifi,
	@Json(name = "fs") val fileSystem : FileSystem,
	@Json(name = "ndc") val ndc : Int,
	@Json(name = "arch") val platformName : String,
	@Json(name = "core") val arduinoCoreVersion : String,
	@Json(name = "lwip") val lwip : Int,
	@Json(name = "freeheap") val freeHeap : Int,
	@Json(name = "uptime") val uptime : Int,
	@Json(name = "opt") val opt : Int,
	@Json(name = "brand") val brand : String,
	@Json(name = "product") val product : String,
	@Json(name = "mac") val mac : String
)