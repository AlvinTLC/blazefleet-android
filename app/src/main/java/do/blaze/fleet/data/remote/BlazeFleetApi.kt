package do.blaze.fleet.data.remote

import do.blaze.fleet.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface BlazeFleetApi {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    @GET("/api/v1/mobile/fleet-summary")
    suspend fun getFleetSummary(): Response<FleetSummaryResponse>

    @POST("/api/v1/mobile/push-token")
    suspend fun registerPushToken(@Body body: MobilePushRegisterRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "/api/v1/mobile/push-token", hasBody = true)
    suspend fun unregisterPushToken(@Body body: MobilePushUnregisterRequest): Response<Unit>

    @POST("/api/v1/mobile/vehicles/{id}/command")
    suspend fun dispatchVehicleCommand(
        @Path("id") vehicleId: String,
        @Body body: RemoteCommandRequest
    ): Response<RemoteCommandResponse>

    @POST("/api/v1/ws/ticket")
    suspend fun getWSTicket(): Response<WSTicketResponse>

    @GET("/api/v1/notifications")
    suspend fun getNotifications(@Query("limit") limit: Int = 30): Response<List<NotificationItem>>

    @GET("/api/v1/mobile/alerts")
    suspend fun getMobileAlerts(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("unacknowledged_only") unacknowledgedOnly: Boolean = false
    ): Response<MobileAlertsResponse>

    @POST("/api/v1/mobile/alerts/{id}/ack")
    suspend fun acknowledgeAlert(
        @Path("id") alertId: String,
        @Body body: AckAlertRequest = AckAlertRequest()
    ): Response<Unit>

    @GET("/api/v1/mobile/metrics")
    suspend fun getMobileMetrics(): Response<MobileMetricsResponse>

    @POST("/api/v1/vehicles/{id}/share-link")
    suspend fun createVehicleShareLink(
        @Path("id") vehicleId: String,
        @Body body: CreateShareLinkRequest
    ): Response<VehicleShareLinkResponse>
}
