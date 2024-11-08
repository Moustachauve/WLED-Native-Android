package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Info (

    @Json(name = "leds") val leds : Leds,
    @Json(name = "wifi") val wifi : Wifi,
    @Json(name = "ver") val version : String? = null,
    @Json(name = "vid") val buildId : Int? = null,
    @Json(name = "name") val name : String,
    @Json(name = "str") val str : Boolean? = null,
    @Json(name = "udpport") val udpPort : Int? = null,
    @Json(name = "live") val isUpdatedLive : Boolean? = null,
    @Json(name = "lm") val lm : String? = null,
    @Json(name = "lip") val lip : String? = null,
    @Json(name = "ws") val websocketClientCount : Int? = null,
    @Json(name = "fxcount") val effectCount : Int? = null,
    @Json(name = "palcount") val paletteCount : Int? = null,
    @Json(name = "fs") val fileSystem : FileSystem? = null,
    @Json(name = "ndc") val ndc : Int? = null,
    @Json(name = "arch") val platformName : String? = null,
    @Json(name = "core") val arduinoCoreVersion : String? = null,
    @Json(name = "lwip") val lwip : Int? = null,
    @Json(name = "freeheap") val freeHeap : Int? = null,
    @Json(name = "uptime") val uptime : Int? = null,
    @Json(name = "opt") val opt : Int? = null,
    @Json(name = "brand") val brand : String? = null,
    @Json(name = "product") val product : String? = null,
    @Json(name = "mac") val mac : String? = null,
    @Json(name = "u") val usermods : Usermods
)