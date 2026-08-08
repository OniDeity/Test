package com.onideity.rommcompanion.data.remote

import com.onideity.rommcompanion.data.remote.dto.CollectionDto
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthInitRequest
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthInitResponse
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthTokenRequest
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthTokenResponse
import com.onideity.rommcompanion.data.remote.dto.DeviceDto
import com.onideity.rommcompanion.data.remote.dto.FirmwareDto
import com.onideity.rommcompanion.data.remote.dto.PlatformDto
import com.onideity.rommcompanion.data.remote.dto.RomPageDto
import com.onideity.rommcompanion.data.remote.dto.SaveDto
import com.onideity.rommcompanion.data.remote.dto.StateDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * RomM's REST API — see https://docs.romm.app/API-and-Development/API-Reference/
 * and the server's own live docs at {baseUrl}/api/docs.
 *
 * Two endpoints (device-pairing init/token) are unauthenticated by design;
 * [AuthInterceptor] skips adding a bearer token for those paths.
 */
interface RommApi {

    // --- Device pairing (unauthenticated) ---

    @POST("api/auth/device/init")
    suspend fun initDevicePairing(@Body payload: DeviceAuthInitRequest): DeviceAuthInitResponse

    /** Returns 400 with a `{"detail": "authorization_pending" | "slow_down" | ...}` body until approved. */
    @POST("api/auth/device/token")
    suspend fun pollDevicePairing(@Body payload: DeviceAuthTokenRequest): Response<DeviceAuthTokenResponse>

    // --- Library ---

    @GET("api/platforms")
    suspend fun getPlatforms(): List<PlatformDto>

    @GET("api/roms")
    suspend fun getRoms(
        @Query("platform_ids") platformId: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): RomPageDto

    @Streaming
    @GET("api/roms/{id}/content/{fileName}")
    suspend fun downloadRomContent(
        @Path("id") romId: Long,
        @Path("fileName") fileName: String,
    ): ResponseBody

    @GET("api/collections")
    suspend fun getCollections(): List<CollectionDto>

    // --- Firmware ---

    @GET("api/firmware")
    suspend fun getFirmware(@Query("platform_id") platformId: Long): List<FirmwareDto>

    @Streaming
    @GET("api/firmware/{id}/content/{fileName}")
    suspend fun downloadFirmwareContent(
        @Path("id") firmwareId: Long,
        @Path("fileName") fileName: String,
    ): ResponseBody

    // --- Saves ---

    @GET("api/saves")
    suspend fun getSaves(
        @Query("rom_id") romId: Long? = null,
        @Query("device_id") deviceId: String? = null,
    ): List<SaveDto>

    @Multipart
    @POST("api/saves")
    suspend fun uploadSave(
        @Query("rom_id") romId: Long,
        @Query("device_id") deviceId: String,
        @Query("emulator") emulator: String?,
        @Query("overwrite") overwrite: Boolean = false,
        @Part saveFile: MultipartBody.Part,
    ): SaveDto

    @Streaming
    @GET("api/saves/{id}/content")
    suspend fun downloadSave(
        @Path("id") saveId: Long,
        @Query("device_id") deviceId: String,
    ): ResponseBody

    @POST("api/saves/{id}/downloaded")
    suspend fun confirmSaveDownloaded(
        @Path("id") saveId: Long,
        @Body body: Map<String, String>,
    ): SaveDto

    // --- Save states ---

    @GET("api/states")
    suspend fun getStates(@Query("rom_id") romId: Long? = null): List<StateDto>

    @Multipart
    @POST("api/states")
    suspend fun uploadState(
        @Query("rom_id") romId: Long,
        @Query("emulator") emulator: String?,
        @Part stateFile: MultipartBody.Part,
    ): StateDto

    @Streaming
    @GET("api/states/{id}/content")
    suspend fun downloadState(@Path("id") stateId: Long): ResponseBody

    // --- Paired devices (manage from within the app) ---

    @GET("api/devices")
    suspend fun getDevices(): List<DeviceDto>

    @DELETE("api/devices/{id}")
    suspend fun revokeDevice(@Path("id") deviceId: String)
}
