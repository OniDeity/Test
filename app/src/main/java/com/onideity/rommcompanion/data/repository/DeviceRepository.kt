package com.onideity.rommcompanion.data.repository

import com.onideity.rommcompanion.data.remote.RommSession
import com.onideity.rommcompanion.data.remote.dto.DeviceDto

/** Lets the user see/manage the RomM devices they've paired, including this one. */
class DeviceRepository(private val session: RommSession) {

    suspend fun listDevices(): List<DeviceDto> = session.requireApi().getDevices()

    suspend fun revokeDevice(deviceId: String) = session.requireApi().revokeDevice(deviceId)
}
