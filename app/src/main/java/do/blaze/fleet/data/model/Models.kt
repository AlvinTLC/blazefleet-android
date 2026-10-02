package do.blaze.fleet.data.model

import com.google.gson.annotations.SerializedName

enum class VehicleState(val displayName: String) {
    @SerializedName("moving") MOVING("En movimiento"),
    @SerializedName("idle") IDLE("En ralentí"),
    @SerializedName("stopped") STOPPED("Detenido"),
    @SerializedName("offline") OFFLINE("Sin señal")
}

data class User(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val role: String
)

data class Tenant(
    val id: String,
    val name: String,
    val slug: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_expires_at") val refreshExpiresAt: String?,
    val user: User,
    val tenant: Tenant
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class WSTicketResponse(
    val ticket: String,
    val url: String
)

data class LivePosition(
    @SerializedName("tracker_id") val trackerId: String,
    @SerializedName("vehicle_id") val vehicleId: String?,
    val plate: String?,
    @SerializedName("vehicle_model") val vehicleModel: String?,
    @SerializedName("driver_name") val driverName: String?,
    val state: VehicleState?,
    val lat: Double?,
    val lng: Double?,
    @SerializedName("speed_kmh") val speedKmh: Double?,
    val course: Double?,
    val ignition: Bool?,
    val satellites: Int?,
    @SerializedName("odometer_km") val odometerKm: Double?,
    @SerializedName("vehicle_odometer_km") val vehicleOdometerKm: Int?,
    val time: String?
)

data class MobileVehicleSummary(
    @SerializedName("vehicle_id") val vehicleId: String,
    @SerializedName("tracker_id") val trackerId: String,
    val plate: String,
    @SerializedName("vehicle_model") val vehicleModel: String,
    @SerializedName("driver_name") val driverName: String?,
    val state: VehicleState,
    val time: String?,
    val lat: Double?,
    val lng: Double?,
    @SerializedName("speed_kmh") val speedKmh: Double?,
    val course: Double?,
    val ignition: Boolean?,
    val satellites: Int?,
    @SerializedName("odometer_km") val odometerKm: Double?,
    @SerializedName("vehicle_odometer_km") val vehicleOdometerKm: Int,
    @SerializedName("battery_pct") val batteryPct: Int?
)

data class FleetSummaryCounts(
    val total: Int,
    val moving: Int,
    val idle: Int,
    val stopped: Int,
    val offline: Int,
    val sos: Int
)

data class FleetSummaryResponse(
    val counts: FleetSummaryCounts,
    val vehicles: List<MobileVehicleSummary>,
    @SerializedName("active_alerts_count") val activeAlertsCount: Int,
    @SerializedName("server_time") val serverTime: String
)

data class MobilePushRegisterRequest(
    val platform: String = "android",
    val token: String,
    @SerializedName("device_name") val deviceName: String?,
    @SerializedName("app_version") val appVersion: String?
)

data class MobilePushUnregisterRequest(
    val token: String
)

data class RemoteCommandRequest(
    val command: String,
    val reason: String?
)

data class RemoteCommandResponse(
    @SerializedName("command_id") val commandId: String,
    @SerializedName("vehicle_id") val vehicleId: String,
    @SerializedName("tracker_id") val trackerId: String,
    val command: String,
    val status: String,
    @SerializedName("dispatched_at") val dispatchedAt: String
)

data class NotificationItem(
    val id: String,
    val kind: String,
    val title: String,
    val body: String,
    @SerializedName("entity_type") val entityType: String?,
    @SerializedName("entity_id") val entityId: String?,
    val read: Boolean,
    @SerializedName("created_at") val createdAt: String
)
