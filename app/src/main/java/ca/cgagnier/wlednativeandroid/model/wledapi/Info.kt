package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Info (

    @Json(name = "leds") val leds : Leds,
    @Json(name = "wifi") val wifi : Wifi,
    @Json(name = "ver") val version : String? = null,
    @Json(name = "vid") val buildId : Int? = null,
    // Added in 0.15
    @Json(name = "cn") val codeName : String? = null,
    // Added in 0.15
    @Json(name = "release") val release : String? = null,
    @Json(name = "name") val name : String,
    @Json(name = "str") val syncToggleReceive : Boolean? = null,
    @Json(name = "udpport") val udpPort : Int? = null,
    // Added in 0.15
    @Json(name = "simplifiedui") val simplifiedUI : Boolean? = null,
    @Json(name = "live") val isUpdatedLive : Boolean? = null,
    @Json(name = "liveseg") val liveSegment : Int? = null,
    @Json(name = "lm") val realtimeMode : String? = null,
    @Json(name = "lip") val realtimeIp : String? = null,
    @Json(name = "ws") val websocketClientCount : Int? = null,
    @Json(name = "fxcount") val effectCount : Int? = null,
    @Json(name = "palcount") val paletteCount : Int? = null,
    @Json(name = "cpalcount") val customPaletteCount : Int? = null,
    // Missing: maps
    @Json(name = "fs") val fileSystem : FileSystem? = null,
    @Json(name = "ndc") val nodeListCount : Int? = null,
    @Json(name = "arch") val platformName : String? = null,
    @Json(name = "core") val arduinoCoreVersion : String? = null,
    // Added in 0.15
    @Json(name = "clock") val clockFrequency : Int? = null,
    // Added in 0.15
    @Json(name = "flash") val flashChipSize : Int? = null,
    @Deprecated("lwip is deprecated and is supposed to be removed in 0.14.0")
    @Json(name = "lwip") val lwip : Int? = null,
    @Json(name = "freeheap") val freeHeap : Int? = null,
    @Json(name = "uptime") val uptime : Int? = null,
    @Json(name = "time") val time : String? = null,
    // Contains some extra options status in the form of a bitset
    @Json(name = "opt") val options : Int? = null,
    @Json(name = "brand") val brand : String? = null,
    @Json(name = "product") val product : String? = null,
    @Json(name = "mac") val macAddress : String? = null,
    @Json(name = "ip") val ipAddress : String? = null,
    @Json(name = "u") val usermods : Usermods? = null
)