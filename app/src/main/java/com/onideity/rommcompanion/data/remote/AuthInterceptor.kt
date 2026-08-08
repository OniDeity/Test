package com.onideity.rommcompanion.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Paths that must NOT get a bearer token — the device isn't paired yet when calling them. */
private val UNAUTHENTICATED_PATHS = setOf(
    "api/auth/device/init",
    "api/auth/device/token",
)

/** Attaches the current bearer token to every authenticated request. [tokenProvider] is read fresh per-request. */
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.removePrefix("/")

        val token = tokenProvider()
        if (path in UNAUTHENTICATED_PATHS || token == null) {
            return chain.proceed(request)
        }

        val authed = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authed)
    }
}
