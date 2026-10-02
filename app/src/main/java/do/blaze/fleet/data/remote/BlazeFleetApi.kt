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
}
