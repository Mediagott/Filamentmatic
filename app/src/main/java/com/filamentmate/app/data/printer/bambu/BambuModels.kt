package com.filamentmate.app.data.printer.bambu

import com.google.gson.annotations.SerializedName

/**
 * Bambu Lab MQTT report message structure.
 * Based on observed payloads from X1C.
 */
data class BambuReport(
    @SerializedName("print") val print: BambuPrintStatus? = null
)

data class BambuPrintStatus(
    @SerializedName("gcode_state") val gcodeState: String? = null,
    @SerializedName("gcode_file") val gcodeFile: String? = null,
    @SerializedName("mc_percent") val progressPercent: Int? = null,
    @SerializedName("mc_remaining_time") val remainingTimeMin: Int? = null,
    @SerializedName("bed_temper") val bedTemp: Float? = null,
    @SerializedName("bed_target_temper") val bedTargetTemp: Float? = null,
    @SerializedName("nozzle_temper") val nozzleTemp: Float? = null,
    @SerializedName("nozzle_target_temper") val nozzleTargetTemp: Float? = null,
    @SerializedName("chamber_temper") val chamberTemp: Float? = null,
    @SerializedName("cooling_fan_speed") val fanSpeed: Int? = null,
    @SerializedName("heatbreak_fan_speed") val heatbreakFanSpeed: Int? = null,
    @SerializedName("layer_num") val currentLayer: Int? = null,
    @SerializedName("total_layer_num") val totalLayers: Int? = null,
    @SerializedName("ams") val ams: BambuAmsData? = null,
    @SerializedName("vt_tray") val vtTray: BambuTrayData? = null,  // External spool holder
    @SerializedName("wifi_signal") val wifiSignal: String? = null,
    @SerializedName("command") val command: String? = null,
    @SerializedName("msg") val errorMsg: Int? = null,
    @SerializedName("sequence_id") val sequenceId: String? = null
)

data class BambuAmsData(
    @SerializedName("ams") val units: List<BambuAmsUnit>? = null,
    @SerializedName("ams_exist_bits") val existBits: String? = null,
    @SerializedName("tray_exist_bits") val trayExistBits: String? = null,
    @SerializedName("tray_now") val trayNow: String? = null,  // Currently active tray
    @SerializedName("tray_tar") val trayTarget: String? = null,
    @SerializedName("version") val version: Int? = null,
    @SerializedName("humidity") val humidity: String? = null
)

data class BambuAmsUnit(
    @SerializedName("id") val id: String? = null,
    @SerializedName("humidity") val humidity: String? = null,
    @SerializedName("temp") val temp: String? = null,
    @SerializedName("tray") val trays: List<BambuTrayData>? = null
)

data class BambuTrayData(
    @SerializedName("id") val id: String? = null,
    @SerializedName("tray_id_name") val trayIdName: String? = null,
    @SerializedName("tray_type") val trayType: String? = null,  // Material type
    @SerializedName("tray_sub_brands") val subBrand: String? = null,
    @SerializedName("tray_color") val trayColor: String? = null,  // Hex RRGGBBAA
    @SerializedName("tray_weight") val trayWeight: String? = null,
    @SerializedName("tray_diameter") val trayDiameter: String? = null,
    @SerializedName("tray_temp") val trayTemp: String? = null,
    @SerializedName("tray_time") val trayTime: String? = null,
    @SerializedName("bed_temp_type") val bedTempType: String? = null,
    @SerializedName("bed_temp") val bedTemp: String? = null,
    @SerializedName("nozzle_temp_max") val nozzleTempMax: String? = null,
    @SerializedName("nozzle_temp_min") val nozzleTempMin: String? = null,
    @SerializedName("remain") val remainPercent: Int? = null,
    @SerializedName("tag_uid") val tagUid: String? = null,
    @SerializedName("tray_uuid") val trayUuid: String? = null,
    @SerializedName("cols") val cols: List<String>? = null  // Color array
)
